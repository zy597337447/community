package com.zhang.community.controller;

import com.sun.org.apache.regexp.internal.RE;
import com.zhang.community.annotation.LoginRequired;
import com.zhang.community.entity.Page;
import com.zhang.community.entity.User;
import com.zhang.community.service.FollowerService;
import com.zhang.community.service.UserService;
import com.zhang.community.util.CommunityConstant;
import com.zhang.community.util.CommunityUtil;
import com.zhang.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowerService followerService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //异步请求
    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody

    public String follow(int entityType , int entityId){
        User user = hostHolder.getUser();

        followerService.follow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"关注成功");
    }

    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType , int entityId){
        User user = hostHolder.getUser();

        followerService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"取消关注成功");
    }

    //关注列表
    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw  new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);


        //设置分页参数
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followerService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        List<Map<String, Object>> followees = followerService.findFollowees(userId, page.getOffset(), page.getLimit());

        //查询关注列表中用户的关注状态
        if (followees != null){
            for (Map<String,Object> map : followees){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followees",followees);

//        model.addAttribute("loginUser",hostHolder.getUser());
        return "site/followee";

    }

    //粉丝列表
    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw  new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);

        //设置分页参数
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followerService.findFollowerCount(ENTITY_TYPE_USER,userId));
        List<Map<String, Object>> followers = followerService.findFollowers(userId, page.getOffset(), page.getLimit());

        //查询粉丝列表中用户的关注状态
        if (followers != null){
            for (Map<String,Object> map : followers){
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followers",followers);
        model.addAttribute("loginUser",hostHolder.getUser());
        return "site/follower";

    }

    private boolean hasFollowed(int userId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followerService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }


}
