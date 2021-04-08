package dev.yxy.controller;

import dev.yxy.annotation.IsAdmin;
import dev.yxy.annotation.IsAnon;
import dev.yxy.annotation.IsAuth;
import dev.yxy.annotation.IsUser;
import dev.yxy.handler.CustomizeAccessDeniedHandler;
import dev.yxy.model.Member;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 自定义权限过滤方法，可以和其他类型混合
     * 在 {@link CustomizeAccessDeniedHandler#handle} 中可以配置拒绝后的返回信息
     */
    @PreAuthorize("permitAll() AND @ss.hasPermission('controller:channel:edit') AND @cs.hasPermission(#member)")
    @PutMapping("/update")
    @ResponseBody
    public String update(@RequestBody Member member) {
        return "更新成功";
    }
}
