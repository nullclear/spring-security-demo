package dev.yxy.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public String login(@RequestHeader(value = "X-Type", required = false) String type) {
        if ("Mobile".equalsIgnoreCase(type)) {
            return "redirect:/mobile/failure";
        } else {
            return "login";
        }
    }

    /**
     * 移动设备登录失败
     * 此路径需要在配置中放行，不然会无限重定向
     */
    @GetMapping("/mobile/failure")
    @ResponseBody
    public String failure() {
        return "移动设备登录失败";
    }

    /**
     * 跳转到首页
     *
     * @return 首页
     */
    @GetMapping("/")
    public String index(@RequestHeader(value = "X-Type", required = false) String type, Model model) {
        if ("Mobile".equalsIgnoreCase(type)) {
            return "redirect:/mobile/success";
        } else {
            model.addAttribute("expectedRole", "ADMIN");
            return "index";
        }
    }

    /**
     * 移动设备登录成功
     */
    @GetMapping("/mobile/success")
    @ResponseBody
    public String success() {
        return "欢迎使用移动设备";
    }

    /**
     * 获取认证信息
     */
    @GetMapping("/info")
    @ResponseBody
    public UserDetails getInfo(Authentication authentication) {
        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        System.out.println("details.getRemoteAddress() = " + details.getRemoteAddress());
        System.out.println("details.getSessionId() = " + details.getSessionId());
        return ((UserDetails) authentication.getPrincipal());
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
