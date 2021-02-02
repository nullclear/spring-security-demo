package dev.yxy.strategy;

import dev.yxy.global.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Nuclear on 2021/2/2
 */
public class CustomizeSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CustomizeSessionInformationExpiredStrategy.class);

    private final String destinationUrl;
    private final RedirectStrategy redirectStrategy;

    public CustomizeSessionInformationExpiredStrategy(String invalidSessionUrl) {
        this(invalidSessionUrl, new DefaultRedirectStrategy());
    }

    public CustomizeSessionInformationExpiredStrategy(String invalidSessionUrl, RedirectStrategy redirectStrategy) {
        Assert.isTrue(UrlUtils.isValidRedirectUrl(invalidSessionUrl), "url must start with '/' or with 'http(s)'");
        this.destinationUrl = invalidSessionUrl;
        this.redirectStrategy = redirectStrategy;
    }

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        HttpServletRequest request = event.getRequest();
        HttpServletResponse response = event.getResponse();
        if ("Mobile".equalsIgnoreCase(request.getHeader("X-Type"))) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);//这个不设置，等会返回的中文乱码
            PrintWriter out = response.getWriter();
            out.write(Response.exception("您的账号已从其他地方登录", event.getSessionInformation()));
            response.flushBuffer();
            out.flush();
            out.close();
        } else {
            logger.debug("Redirecting to '" + destinationUrl + "'");
            redirectStrategy.sendRedirect(request, response, destinationUrl);
        }
    }
}
