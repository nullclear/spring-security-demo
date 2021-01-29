package dev.yxy.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yxy.handler.CustomizeAuthenticationFailureHandler;
import dev.yxy.handler.CustomizeAuthenticationSuccessHandler;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 1. 首先登录请求肯定是 POST，如果不是 POST ，直接抛出异常，后面的也不处理了。
 * 2. 因为要在这里处理验证码，所以第二步从 session 中把已经下发过的验证码的值拿出来。
 * 3. 接下来通过 contentType 来判断当前请求是否通过 JSON 来传递参数，
 * 如果是通过 JSON 传递参数，则按照 JSON 的方式解析，
 * 如果不是，则调用 super.attemptAuthentication 方法，进入父类的处理逻辑中，也就是说，
 * 我们自定义的这个类，既支持 JSON 形式传递参数，也支持 key/value 形式传递参数。
 * 4. 如果是 JSON 形式的数据，我们就通过读取 request 中的 I/O 流，将 JSON 映射到一个 Map 上。
 * 5. 从 Map 中取出 captcha，先去判断验证码是否正确，如果验证码有错，则直接抛出异常。
 * 6. 接下来从 Map 中取出 username 和 password，构造 UsernamePasswordAuthenticationToken 对象并作校验。
 * Created by Nuclear on 2021/1/28
 */
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    public LoginFilter(AuthenticationManager authenticationManager) {
        this.setAuthenticationSuccessHandler(new CustomizeAuthenticationSuccessHandler("/"));
        this.setAuthenticationFailureHandler(new CustomizeAuthenticationFailureHandler("/auth?error=true"));
        this.setFilterProcessesUrl("/login");
        this.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String captcha = (String) request.getSession().getAttribute("captcha");

        if (request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE) || request.getContentType().equals(MediaType.APPLICATION_JSON_UTF8_VALUE)) {
            Map<String, String> loginData = new HashMap<>();
            try {
                //noinspection unchecked
                loginData = (Map<String, String>) (new ObjectMapper().readValue(request.getInputStream(), Map.class));
            } catch (IOException e) {
                throw new AuthenticationServiceException("登录数据解析错误");
            } finally {
                String requestCaptcha = loginData.get("captcha");
                checkCode(requestCaptcha, captcha);
            }
            String username = loginData.get(getUsernameParameter());
            String password = loginData.get(getPasswordParameter());
            if (username == null) {
                username = "";
            }
            if (password == null) {
                password = "";
            }
            username = username.trim();
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        } else {
            checkCode(request.getParameter("captcha"), captcha);
            return super.attemptAuthentication(request, response);
        }
    }

    public void checkCode(String requestCaptcha, String captcha) {
        if (StringUtils.isEmpty(requestCaptcha) || StringUtils.isEmpty(captcha) || !requestCaptcha.equalsIgnoreCase(captcha)) {
            //验证码不正确
            throw new AuthenticationServiceException("验证码不正确");
        }
    }
}
