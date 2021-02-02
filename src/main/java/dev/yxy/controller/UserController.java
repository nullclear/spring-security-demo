package dev.yxy.controller;

import dev.yxy.global.Response;
import dev.yxy.model.Member;
import dev.yxy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nuclear on 2021/2/2
 */
@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 获取认证信息
     */
    @GetMapping("/info")
    @ResponseBody
    public UserDetails getInfo(Authentication authentication) {
        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        logger.info("sessionId：{}   remoteAddress：{}", details.getSessionId(), details.getRemoteAddress());
        return ((UserDetails) authentication.getPrincipal());
    }

    /**
     * 修改认证信息
     */
    @PutMapping("/info")
    @ResponseBody
    public UserDetails updateInfo(Authentication authentication) {
        // 更新信息
        Member member = (Member) authentication.getPrincipal();
        member.setVersion(member.getVersion() + 1);
        // 生成新Token
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(member, authentication.getCredentials(), authentication.getAuthorities());
        token.setDetails(authentication.getDetails());
        // 放到Spring Security的上下文中，如果使用了spring session，也会自动刷新redis中数据
        SecurityContextHolder.getContext().setAuthentication(token);
        return member;
    }

    /**
     * 获取其他认证用户的session信息
     */
    @GetMapping("/session")
    @ResponseBody
    public HashMap<String, HashMap<String, Object>> findRemoteAddressByUsername() {
        //获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = (Member) authentication.getPrincipal();
        //根据用户名查询所有在线Session
        Map<String, ? extends Session> map = userService.findByIndexNameAndIndexValue(member.getUsername());
        HashMap<String, HashMap<String, Object>> hashMap = new HashMap<>();
        for (Session session : map.values()) {
            HashMap<String, Object> content = new HashMap<>();
            content.put("id", session.getId());
            content.put("creationTime", session.getCreationTime());
            content.put("lastAccessedTime", session.getLastAccessedTime());
            content.put("maxInactiveInterval", String.format("%dS", session.getMaxInactiveInterval().getSeconds()));
            for (String attributeName : session.getAttributeNames()) {
                if (attributeName != null && !attributeName.startsWith("SPRING_SECURITY")) {
                    content.put(attributeName, session.getAttributeOrDefault(attributeName, new Object()));
                }
            }
            hashMap.put(session.getId(), content);
        }
        return hashMap;
    }

    /**
     * 删除其他认证用户的session信息
     */
    @DeleteMapping(path = "/session", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String deleteOther(HttpSession session, Principal principal) {
        try {
            int result = userService.deleteOther(session.getId(), principal.getName());
            return Response.success("删除其他用户的Session成功", result);
        } catch (Exception e) {
            return Response.exception("删除Session时出现错误");
        }
    }
}
