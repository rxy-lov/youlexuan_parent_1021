package com.offcn.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 获取登录的名字返回Map
     */
    @RequestMapping("/name")
    public Map name(){
        Map map = new HashMap();

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put( "loginName", name );
        return map;
    }
}
