package com.zhang.community.service;

import com.zhang.community.dao.LoginTicketMapper;
import com.zhang.community.dao.UserMapper;
import com.zhang.community.entity.LoginTicket;
import com.zhang.community.entity.User;
import com.zhang.community.util.CommunityConstant;
import com.zhang.community.util.CommunityUtil;
import com.zhang.community.util.MailClient;
import com.zhang.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sun.security.pkcs11.P11Util;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;


    public User findUserById(int id){
        User user = getCache(id);
        if (user == null){
            user = userMapper.selectById(id);
        }
        return user;
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if (user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","账号不能为空");
            return map;
        }
        //验证账号
        User user1 = userMapper.selectByName(user.getUsername());
        if (user1!=null){
            map.put("usernameMsg","该账号已存在");
            return map;
        }
        //验证邮箱
        user1 = userMapper.selectByEmail(user.getEmail());
        if (user1!=null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.MD5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //激活地址：http://localhost:8080/community/activation/101/Activationcode
        String url = domain + contextPath + "/activation/" + user.getId() +"/"+ user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;


    }
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,int expiredSeconds){

        Map<String,Object> map =new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }else if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }
        //验证状态
        if (user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }

        //验证密码
        String salt = user.getSalt();
        String pass = CommunityUtil.MD5(password+salt);
        if (!user.getPassword().equals(pass)){
            map.put("passwordMsg","密码错误");
            return map;
        }

        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds *1000));

//        loginTicketMapper.insertLoginTicket(loginTicket);

        //存到redis里
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;

    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }


    public Map<String,Object> resetPassword(String email,String password){
//        Map<String,Object> map= new HashMap<>();
//        if (StringUtils.isBlank(password)){
//            map.put("passwordMsg","密码不能为空");
//            return map;
//        }
//        if (password.equals(userMapper.selectByEmail(email).getPassword())){
//            map.put("passwordMsg","新旧密码不能重复");
//        }
//
//        User user = userMapper.selectByEmail(email);
//        String newPassword = CommunityUtil.MD5(password+user.getSalt());
//        userMapper.updatePassword(user.getId(),newPassword);
//        map.put("user",user);
//
//
//        return map;
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱尚未注册!");
            return map;
        }

        // 重置密码
        password = CommunityUtil.MD5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);
        clearCache(user.getId());
        map.put("user", user);
        return map;
    }

    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
//        return loginTicketMapper.selectByTicket(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }


    public int updateHeaderUrl(int userId,String headerUrl){
//        return userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;

    }


    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }


    //1、优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }


    //2、取不到时初始化缓存数据
    private User initCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        User user = userMapper.selectById(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }


    //3、数据发生变更时清除缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

}
