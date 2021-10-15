package dev.yxy.handler;

import dev.yxy.global.Response;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

/**
 * 自定义认证失败处理器
 * <p>
 * Created by Nuclear on 2021/1/29
 */
public class CustomizeAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public CustomizeAuthenticationFailureHandler(String defaultFailureUrl) {
        super(defaultFailureUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //如果是移动设备，就返回Json数据，如果不是移动设备，就调用父类的逻辑
        if ("Mobile".equalsIgnoreCase(request.getHeader("X-Type"))) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            PrintWriter out = response.getWriter();
            String result;
            if (exception instanceof LockedException) {
                result = Response.exception("账户被锁定，请联系管理员!");
            } else if (exception instanceof CredentialsExpiredException) {
                result = Response.exception("密码过期，请联系管理员!");
            } else if (exception instanceof AccountExpiredException) {
                result = Response.exception("账户过期，请联系管理员!");
            } else if (exception instanceof DisabledException) {
                result = Response.exception("账户被禁用，请联系管理员!");
            } else if (exception instanceof BadCredentialsException) {
                result = Response.exception("用户名或者密码输入错误，请重新输入!");
            } else {
                result = Response.exception(exception.getMessage());
            }
            out.write(result);
            out.flush();
            out.close();
        } else {
            // todo 失败网页的跳转结果 可以在这里改
            super.setDefaultFailureUrl("/auth?error=true&msg=" + URLEncoder.encode(exception.getMessage(), "UTF-8"));
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
