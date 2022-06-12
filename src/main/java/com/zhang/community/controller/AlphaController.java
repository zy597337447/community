package com.zhang.community.controller;

import com.sun.javafx.collections.MappingChange;
import com.zhang.community.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @ResponseBody
    @GetMapping("/emp")
    public Map<String,String> emp(){
        Map<String,String> map = new HashMap<>();
        map.put("name","zhangyu");
        return map;
    }


    @ResponseBody
    @GetMapping("/hello")
    public ModelAndView emp2(){
        ModelAndView modelAndView = new ModelAndView();
        Map<String,Object> map = new HashMap<>();
        map.put("age",12);
        map.put("gender","male");
        map.put("height",180);
        modelAndView.addObject("name","张御");
        modelAndView.addAllObjects(map);
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    @RequestMapping(path = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie生效的范围
        cookie.setPath("/community");
        //设置cookie生效的时间
        cookie.setMaxAge(60 * 10);
        response.addCookie(cookie);
        return "set cookie";
    }

    @RequestMapping(path = "/cookie/get",method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code")String code){
        System.out.println(code);
        return "get cookie";
    }
    @RequestMapping(path = "session/set",method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("hello","world");
        return "set session";
    }

    @RequestMapping(path = "session/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        return session.getAttribute("hello").toString();
    }

    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功！");
    }

}
