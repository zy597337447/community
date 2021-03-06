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
            model.addAttribute("error","?????????????????????");
            return "/site/setting";
        }
        String originalFilename = headerImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","??????????????????");
            return "/site/setting";
        }
        //?????????????????????
        String fileName = CommunityUtil.generateUUID() + suffix;
        //???????????????????????????
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("??????????????????" + e.getMessage());
            throw new RuntimeException("??????????????????????????????????????????"+ e);
        }

        //???????????????????????????????????????web???????????????
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeaderUrl(user.getId(),headerUrl);

        return "redirect:/index";
    }




    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //?????????????????????
        fileName = uploadPath +  "/" + fileName;
        //????????????
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //????????????
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
            logger.error("??????????????????" + e.getMessage());
        }
    }


    //????????????
    @RequestMapping(path= "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
         User user = userService.findUserById(userId);
         if (user == null){
             throw new RuntimeException("?????????????????????");
         }

         //??????
         model.addAttribute("user",user);

         //???????????????
         int likeCount = likeService.findUserLikeCount(userId);
         model.addAttribute("likeCount",likeCount);

         //????????????
        long followeeCount = followerService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        //????????????
        long followerCount = followerService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //??????????????????????????????????????????
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
