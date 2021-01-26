package dev.yxy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by Nuclear on 2021/1/26
 */
@Controller
public class HelloController {

    /**
     * 跳转到登录页面
     *
     * @return 登录页面
     */
    @GetMapping("/auth")
    public String login() {
        return "login";
    }

    /**
     * 跳转到首页
     *
     * @return 首页
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("expectedRole", "ADMIN");
        return "index";
    }

    /**
     * 跳转到通道页面  {@link ChannelController}
     *
     * @return 通道页面
     */
    @GetMapping("/channel")
    public String channel() {
        return "channel";
    }

    /**
     * 跳转到通道S页面 {@link ChannelSController}
     *
     * @return 通道S页面
     */
    @GetMapping("/channelS")
    public String channelS() {
        return "channelS";
    }

    /**
     * 跳转到通道T页面 {@link ChannelTController}
     *
     * @return 通道T页面
     */
    @GetMapping("/channelT")
    public String channelT() {
        return "channelT";
    }
}
