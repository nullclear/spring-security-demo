package dev.yxy.details;

import dev.yxy.controller.HelloController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * 这个也能用来处理验证码
 * 这个类的父类就是 {@link HelloController}#getInfo() 里获取到的details的来源
 * Created by atom on 2021/2/1
 */
public class CustomizeWebAuthenticationDetails extends WebAuthenticationDetails {
    private static final Logger logger = LoggerFactory.getLogger(CustomizeWebAuthenticationDetails.class);

    /**
     * todo
     * 父类 {@link WebAuthenticationDetails}里sessionId 可能是null，所以用这个获取SessionId是不对的
     */
    public CustomizeWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        String requestCode = request.getParameter("captcha");
        String captcha = (String) request.getSession().getAttribute("captcha");
        logger.info("CustomizeWebAuthenticationDetails：{}", requestCode);
        //throw new AuthenticationServiceException("这里可以抛出异常");
    }
}
