package com.zhang.community.config;

import com.zhang.community.annotation.LoginRequired;
import com.zhang.community.controller.interceptor.AlphaInterceptor;
import com.zhang.community.controller.interceptor.LoginRequiredInterceptor;
import com.zhang.community.controller.interceptor.LoginTicketInterceptor;
import com.zhang.community.entity.LoginTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;
    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor).excludePathPatterns("/**/*.css","/**/*.png","/**/*.jpeg").addPathPatterns("/register","/login");
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.png","/**/*.jpeg","/**/*.js","/**/*.jpg","/**/*.png");
        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.png","/**/*.jpeg","/**/*.js","/**/*.jpg","/**/*.png");
    }






}
