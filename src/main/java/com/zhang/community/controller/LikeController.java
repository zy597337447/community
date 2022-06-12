package com.zhang.community.controller;

import com.zhang.community.entity.User;
import com.zhang.community.service.LikeService;
import com.zhang.community.util.CommunityUtil;
import com.zhang.community.util.HostHolder;
import javafx.geometry.Pos;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType ,int entityId,int entityUserId){
        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //统计点赞数量
        long entityLikeCount = likeService.findEntityLikeCount(entityType, entityId);
        //查询点赞状态
        int entityLikeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String,Object> map = new HashMap<>();
        map.put("entityLikeCount",entityLikeCount);
        map.put("entityLikeStatus",entityLikeStatus);

        return CommunityUtil.getJSONString(0,null,map);
    }
}
