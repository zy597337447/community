package com.zhang.community.controller;

import com.zhang.community.annotation.LoginRequired;
import com.zhang.community.entity.User;
import com.zhang.community.service.FollowerService;
import com.zhang.community.service.LikeService;
import com.zhang.community.service.UserService;
import com.zhang.community.util.CommunityConstant;
import com.zhang.community.util.CommunityUtil;
import com.zhang.community.util.CookieUtil;
import com.zhang.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.jws.WebParam;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;





@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowerService followerService;




    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET )
    public String getSettingPage(){

        return "/site/setting";
    }







    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error","您还未选择图片");
            return "/site/setting";
        }
        String originalFilename = headerImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式错误");
            return "/site/setting";
        }
        //生成随机文件名
        String fileName = CommunityUtil.generateUUID() + suffix;
        //确认文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常"+ e);
        }

        //更新当前用户的头像的路径（web访问路径）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeaderUrl(user.getId(),headerUrl);

        return "redirect:/index";
    }




    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath +  "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + suffix);
        try (
                ServletOutputStream os = response.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(fileName);
                ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1){
                os.write(buffer,0,b);
            }
            fileInputStream.close();

        } catch (IOException e) {
            logger.error("读取图像失败" + e.getMessage());
        }
    }


    //个人主页
    @RequestMapping(path= "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
         User user = userService.findUserById(userId);
         if (user == null){
             throw new RuntimeException("该用户不存在！");
         }

         //用户
         model.addAttribute("user",user);

         //获得点赞数
         int likeCount = likeService.findUserLikeCount(userId);
         model.addAttribute("likeCount",likeCount);

         //关注数量
        long followeeCount = followerService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        //粉丝数量
        long followerCount = followerService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //当前登录用户是否已关注该实体
        boolean hasFollowed = false;
        User loginUser = hostHolder.getUser();
        if (loginUser!=null){
            hasFollowed = followerService.hasFollowed( loginUser.getId(), ENTITY_TYPE_USER,userId);
        }

        model.addAttribute("hasFollowed",hasFollowed);
        model.addAttribute("loginUser",loginUser);


        return "/site/profile";

    }

}
