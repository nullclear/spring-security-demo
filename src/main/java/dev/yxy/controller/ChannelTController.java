package dev.yxy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

/**
 * 使用jsr250注解
 * Created by Nuclear on 2021/1/26
 */
@Controller
@RequestMapping("/channelT")
public class ChannelTController {

    @RolesAllowed("ROLE_USER")
    @GetMapping("/user")
    @ResponseBody
    public String requestUser() {
        return "用户通道T";
    }

    @RolesAllowed("ROLE_ADMIN")
    @GetMapping("/admin")
    @ResponseBody
    public String requestAdmin() {
        return "管理员通道T";
    }

    @RolesAllowed({"ROLE_ADMIN", "ROLE_USER"})
    @GetMapping("/common")
    @ResponseBody
    public String requestAuth() {
        return "管理员和用户通道T";
    }

    /**
     * 任何人都能访问，不管有没有登录，有没有权限
     */
    @PermitAll
    @GetMapping("/anon")
    @ResponseBody
    public String requestAnonymously() {
        return "匿名通道T";
    }
}
