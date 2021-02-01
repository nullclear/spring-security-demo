package dev.yxy.filter;

import dev.yxy.config.WebSecurityConfig;
import dev.yxy.handler.CustomizeAuthenticationFailureHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * 自定义验证码过滤器
 * Created by Nuclear on 2021/1/28
 */
@Component
public class CaptchaFilter extends GenericFilter {
    private static final Logger logger = LoggerFactory.getLogger(CaptchaFilter.class);

    /**
     * 参考这个类中的配置，过滤器会对每个请求都过滤，但是我们不是每个请求都需要处理
     * {@link WebSecurityConfig} loginProcessingUrl("/login")
     */
    public static final String defaultFilterProcessUrl = "/login";

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        //设备类型判断
        if ("Mobile".equalsIgnoreCase(request.getHeader("X-Type"))) {
            //todo 暂时没想到需要干啥
        } else {
            //判断是否是需要拦截的登录请求
            if ("POST".equalsIgnoreCase(request.getMethod()) && defaultFilterProcessUrl.equals(request.getServletPath())) {
                //如果不移动设备类型，就走验证码流程
                //----  模拟Cookie-redis取验证码操作 /start ----
                //todo 从redis中根据 key=PREFIX:token 去取code 然后下面比对逻辑一致了
                String token;
                for (Cookie cookie : Objects.requireNonNullElse(request.getCookies(), new Cookie[0])) {
                    if ("CAPTCHA".equalsIgnoreCase(cookie.getName())) {
                        token = cookie.getValue();
                        logger.info("验证码Token：{}", token);
                    }
                }
                //String captcha = redis.get("PREFIX:token")
                //----  模拟Cookie-redis取验证码操作 /end ----

                //----  模拟session取验证码操作 /start ----
                //todo 获取服务端存在session里的验证码
                String captcha = (String) request.getSession().getAttribute("captcha");
                //----  模拟session取验证码操作 /end ----

                //校验服务端的验证码
                if (StringUtils.isEmpty(captcha)) {
                    logger.warn("服务端验证码已过期或不存在");
                    //设置状态为永久重定向
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    //重定向地址
                    response.sendRedirect("/auth?captcha=true");
                    //截断方法逻辑
                    return;
                }

                //获取客户端传来的验证码
                String requestCode = request.getParameter("captcha");
                if (StringUtils.isEmpty(requestCode)) {
                    logger.warn("验证码不能为空");
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.sendRedirect("/auth?captcha=true");
                    return;
                }

                //校验验证码
                if (!captcha.equalsIgnoreCase(requestCode)) //noinspection DanglingJavadoc
                {
                    //throw new AuthenticationServiceException("验证码错误");
                    /**
                     * todo 这里有个问题
                     * 如果在这里抛出异常 和 在 {@link LoginFilter} 中抛出异常是不一样的，
                     * 这里抛出的异常不会被传递到 {@link CustomizeAuthenticationFailureHandler }
                     */
                    logger.warn("验证码错误");
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.sendRedirect("/auth?captcha=true");
                    return;
                }
            }
        }

        //如果前面执行都没问题，才会继续往下执行
        chain.doFilter(request, response);
    }
}
