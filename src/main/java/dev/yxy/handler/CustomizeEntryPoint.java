package dev.yxy.handler;

import dev.yxy.global.Response;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 未认证处理方案
 * <p>
 * Created by Nuclear on 2021/1/29
 */
public class CustomizeEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public CustomizeEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //如果是移动设备，就返回Json数据，如果不是移动设备，就调用父类的逻辑
        if ("Mobile".equalsIgnoreCase(request.getHeader("X-Type"))) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            PrintWriter out = response.getWriter();
            out.write(Response.exception(401, "未认证，请先登录"));
            out.flush();
            out.close();
        } else {
            super.commence(request, response, exception);
        }
    }
}
