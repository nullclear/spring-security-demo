package dev.yxy.handler;

import dev.yxy.global.Response;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义认证成功处理器
 * <p>
 * Created by Nuclear on 2021/1/29
 */
public class CustomizeAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public CustomizeAuthenticationSuccessHandler(String defaultTargetUrl) {
        this.setDefaultTargetUrl(defaultTargetUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //如果是移动设备，就返回Json数据，如果不是移动设备，就调用父类的逻辑
        if ("Mobile".equalsIgnoreCase(request.getHeader("X-Type"))) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            PrintWriter out = response.getWriter();
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            out.write(Response.success(principal));
            out.flush();
            out.close();
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
