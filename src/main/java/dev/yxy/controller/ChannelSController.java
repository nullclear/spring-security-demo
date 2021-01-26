package dev.yxy.controller;

import dev.yxy.annotation.IsAdmin;
import dev.yxy.annotation.IsAnon;
import dev.yxy.annotation.IsAuth;
import dev.yxy.annotation.IsUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 使用perPost注解
 * Created by Nuclear on 2021/1/26
 */
@Controller
@RequestMapping("/channelS")
public class ChannelSController {

    @IsUser
    @GetMapping("/user")
    @ResponseBody
    public String requestUser() {
        return "用户通道S";
    }

    @IsAdmin
    @GetMapping("/admin")
    @ResponseBody
    public String requestAdmin() {
        return "管理员通道S";
    }

    @IsAuth
    @GetMapping("/common")
    @ResponseBody
    public String requestAuth() {
        return "管理员和用户通道S";
    }

    /**
     * 任何人都能访问，不管有没有登录，有没有权限
     */
    @IsAnon
    @GetMapping("/anon")
    @ResponseBody
    public String requestAnonymously() {
        return "匿名通道S";
    }
}
