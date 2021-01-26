package dev.yxy.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 使用@Secured注解
 * Created by Nuclear on 2021/1/26
 */
@Controller
@RequestMapping("/channel")
public class ChannelController {

    @Secured("ROLE_USER")
    @GetMapping("/user")
    @ResponseBody
    public String requestUser() {
        return "用户通道";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/admin")
    @ResponseBody
    public String requestAdmin() {
        return "管理员通道";
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    @GetMapping("/common")
    @ResponseBody
    public String requestAuth() {
        return "管理员和用户通道";
    }

    /**
     * 任何人都能访问，不管有没有登录，有没有权限
     */
    @Secured("IS_AUTHENTICATED_ANONYMOUSLY")
    @GetMapping("/anon")
    @ResponseBody
    public String requestAnonymously() {
        return "匿名通道";
    }
}
