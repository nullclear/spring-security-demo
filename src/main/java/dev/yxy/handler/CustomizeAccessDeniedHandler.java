package dev.yxy.handler;

import dev.yxy.global.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 授权异常处理器
 * Created by Nuclear on 2021/2/20
 */
public class CustomizeAccessDeniedHandler extends AccessDeniedHandlerImpl {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        //如果是移动设备，就返回Json数据，如果不是移动设备，就调用父类的逻辑
        if ("Mobile".equalsIgnoreCase(request.getHeader("X-Type"))) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            PrintWriter out = response.getWriter();
            out.write(Response.exception(403, "无权限，禁止访问"));
            out.flush();
            out.close();
        } else {
            super.handle(request, response, accessDeniedException);
        }
    }
}
