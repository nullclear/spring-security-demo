package dev.yxy.controller;

import dev.yxy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nuclear on 2021/2/2
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/find")
    @ResponseBody
    public HashMap<String, String> find(Principal principal) {
        Map<String, ? extends Session> map = userService.findByIndexNameAndIndexValue(principal.getName());
        HashMap<String, String> hashMap = new HashMap<>();
        for (Session session : map.values()) {
            hashMap.put(session.getId(), session.getAttribute("remoteAddress"));
        }
        return hashMap;
    }
}
