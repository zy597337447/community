package com.zhang.community.controller;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.zhang.community.dao.UserMapper;
import com.zhang.community.entity.LoginTicket;
import com.zhang.community.entity.User;
import com.zhang.community.service.UserService;
import com.zhang.community.util.CommunityUtil;
import com.zhang.community.util.MailClient;
import com.zhang.community.util.RedisKeyUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReactiveSetCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.jws.WebParam;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.zhang.community.util.CommunityConstant.*;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;



//    private DefaultKaptcha defaultKaptcha;

    @RequestMapping(path = "register" ,method= RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/login" ,method= RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }


    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送一封激活邮件，请及时激活。");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));

            return "/site/register";
        }
    }


    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activate(Model model, @PathVariable("userId") Integer userId, @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用。");
            model.addAttribute("target","/login");
        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，该激活码已被使用。");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败，您提供的激活码不正确。");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response /*HttpSession session */){
        //生成验证码
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);

//        //验证码存入session
//        session.setAttribute("kaptcha",text);

        //验证码的归属(给出一个凭证存到cookie里)
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }


    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code, boolean rememberme,
                        Model model,HttpServletResponse response /*HttpSession session,*/, @CookieValue("kaptchaOwner") String kaptchaOwner){

        //检查验证码(用cookie中保存的凭证生成rediskey从redis获取验证码)
//        String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)|| !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";
        }

        //检查账号密码
        int expiredSeconds = rememberme?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }

    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }



    @RequestMapping(path = "/forget",method = RequestMethod.GET)

    public String getForgetPage(){
        return "/site/forget";
    }

    @RequestMapping(path = "/forget/code",method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, HttpSession session, Model model){
//        Map<String,Object> map = new HashMap<>();
//        if (StringUtils.isBlank(email)){
//            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
//        }
//
//        //验证邮箱
//        User user = userMapper.selectByEmail(email);
//        if (user == null){
//            model.addAttribute("emailMsg","该邮箱不存在");
//        }
//
//        //验证状态
//        if (user.getStatus() == 0){
//            model.addAttribute("emailMsg","该邮箱未激活");
//        }
//
//        //发送验证码
//        String generaCode = CommunityUtil.generateUUID();
//        Context context = new Context();
//        context.setVariable("email",email);
//        context.setVariable("code",generaCode);
//        String content = templateEngine.process("/mail/forget", context);
//        mailClient.sendMail(email,"重置密码",content);
//
//        //保存验证码
//        session.setAttribute("generacode",generaCode);
//        return CommunityUtil.getJSONString(0);
        if (StringUtils.isBlank(email)) {
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }

        // 发送邮件
        Context context = new Context();
        context.setVariable("email", email);
        String code = CommunityUtil.generateUUID().substring(0, 4);
        context.setVariable("verifyCode", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);

        // 保存验证码
        session.setAttribute("verifyCode", code);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 重置密码
     */

    @RequestMapping(path = "/forget/password",method = RequestMethod.POST)
    public String resetPassword(String verifyCode , HttpSession session, Model model,String password,String email){
//        String generacode = (String) session.getAttribute("generacode");
//        if (StringUtils.isBlank(generacode)||StringUtils.isBlank(code)||!code.equalsIgnoreCase(generacode)){
//            model.addAttribute("codeMsg","验证码错误");
//            return "site/forget";
//        }
//
//        Map<String,Object> map = userService.resetPassword(email,password);
//        if (map.containsKey("user")){
//            return "redirect:/login";
//        }else{
//            model.addAttribute("passwordMsg",map.get("passwordMsg"));
//            return "site/forget";
//        }
        String code = (String) session.getAttribute("verifyCode");
        if (StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode)) {
            model.addAttribute("codeMsg", "验证码错误!");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")) {
            return "redirect:/login";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }
}
