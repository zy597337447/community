package com.zhang.community.controller;

import com.zhang.community.entity.DiscussPost;
import com.zhang.community.entity.Page;
import com.zhang.community.entity.User;
import com.zhang.community.service.DiscussPostService;
import com.zhang.community.service.LikeService;
import com.zhang.community.service.UserService;
import com.zhang.community.util.CommunityConstant;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sun.dc.pr.PRError;

import java.util.*;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;




    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        //方法调用之前，springmvc会自动实例化model和page，并将page注入model
        //所以thymeleaf中可以直接访问page对象中的数据，而不需要addAttribute
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> lists = discussPostService.findNewDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (lists!=null){
            for(DiscussPost post:lists){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

}
