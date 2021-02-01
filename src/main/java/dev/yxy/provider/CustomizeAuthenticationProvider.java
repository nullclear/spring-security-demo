package dev.yxy.provider;

import dev.yxy.filter.CaptchaFilter;
import dev.yxy.filter.LoginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 增强验证逻辑，在验证用户信息之前，先检查验证码，当然这里没有写
 * 为什么要这么做呢？
 * {@link CaptchaFilter} 有一个非常大的问题，就是添加到spring security的过滤链中以后，之后的每一个访问都会调用此过滤器
 * {@link LoginFilter} 也有问题，因为自己完全替换了整个认证过滤器，导致原来的功能失效，也不是说不能这么用，只是本来由框架帮你实现的功能
 * 都需要自己去实现，比如这个就缺少了Remember-me的功能，也能做，但是非常麻烦
 * Created by Nuclear on 2021/2/1
 */
public class CustomizeAuthenticationProvider extends DaoAuthenticationProvider {
    private static final Logger logger = LoggerFactory.getLogger(CustomizeAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            String requestCode = request.getParameter("captcha");
            String captcha = (String) request.getSession().getAttribute("captcha");
            logger.info("CustomizeAuthenticationProvider: {}", requestCode);
        }
        return super.authenticate(authentication);
    }
}
