package dev.yxy.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yxy.config.WebSecurityConfig;
import dev.yxy.handler.CustomizeAuthenticationFailureHandler;
import dev.yxy.handler.CustomizeAuthenticationSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 1. 首先登录请求肯定是 POST，如果不是 POST ，直接抛出异常，后面的也不处理了。
 * <p>
 * 2. 因为要在这里处理验证码，所以第二步从 session 中把已经下发过的验证码的值拿出来。
 * <p>
 * 3. 接下来通过 contentType 来判断当前请求是否通过 JSON 来传递参数，
 * *  如果是通过 JSON 传递参数，则按照 JSON 的方式解析，
 * *  如果不是，则调用 super.attemptAuthentication 方法，进入父类的处理逻辑中，也就是说，
 * *  我们自定义的这个类，既支持 JSON 形式传递参数，也支持 key/value 形式传递参数。
 * <p>
 * 4. 如果是 JSON 形式的数据，我们就通过读取 request 中的 I/O 流，将 JSON 映射到一个 Map 上。
 * <p>
 * 5. 从 Map 中取出 captcha，先去判断验证码是否正确，如果验证码有错，则直接抛出异常。
 * <p>
 * 6. 接下来从 Map 中取出 username 和 password，构造 UsernamePasswordAuthenticationToken 对象并作校验。
 * ----------------------------
 * 这里有个问题，这个过滤器只有在用户登录的时候触发，即这里配置的POST /login
 * 但是{@link CaptchaFilter}在验证前，不管什么请求都会触发，所以我们要自己过滤下请求路径
 * <p>
 * Created by Nuclear on 2021/1/28
 */
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

    private static final String httpMethod = "POST";


    /**
     * 当我们代替了 {@link UsernamePasswordAuthenticationFilter} 之后，
     * 原本在 {@link WebSecurityConfig} #configure(HttpSecurity http) 方法中关于 formLogin() 表单的配置就会失效，
     * 那些失效的属性，都可以在配置 LoginFilter 实例的时候配置。
     */
    public LoginFilter(AuthenticationManager authenticationManager) {
        // 登录表单的action地址 和 请求方式，这个/login不配置放行也没事
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login", httpMethod));
        // 认证管理器
        this.setAuthenticationManager(authenticationManager);
        // 自定义认证成功处理器
        this.setAuthenticationSuccessHandler(new CustomizeAuthenticationSuccessHandler("/"));
        // 自定义认证失败处理器
        // todo 这个有个问题，如果采用网页跳转方式，那这个auth其实也会被拦截，需要在路径匹配规则里放行
        this.setAuthenticationFailureHandler(new CustomizeAuthenticationFailureHandler("/auth?error=true"));
        // 可以自定义参数名称
        this.setUsernameParameter("username");
        // 可以自定义参数名称
        this.setPasswordParameter("password");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 判断请求方式
        if (!httpMethod.equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("[LoginFilter] Authentication method not supported: " + request.getMethod());
        }

        logger.info("请求登录的IP：[{}]", request.getRemoteAddr());

        // 获取session中的验证码，当然这个可以改成从redis获取验证码，参考CaptchaFilter
        String captcha = (String) request.getSession().getAttribute("captcha");

        // 如果是json方式请求
        if (Objects.equals(MediaType.APPLICATION_JSON_VALUE, request.getContentType()) || Objects.equals(MediaType.APPLICATION_JSON_UTF8_VALUE, request.getContentType())) {
            Map<String, String> loginData = new HashMap<>();
            try {
                // 将请求的数据映射到Map里
                //noinspection unchecked
                loginData = (Map<String, String>) (new ObjectMapper().readValue(request.getInputStream(), Map.class));
            } catch (IOException e) {
                throw new AuthenticationServiceException("[LoginFilter] 登录数据解析错误");
            } finally {
                // 检查验证码
                checkCode(loginData.get("captcha"), captcha);
            }
            String username = loginData.get(getUsernameParameter());
            String password = loginData.get(getPasswordParameter());
            // 下面与父类的操作一致了
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
            // 检查验证码
            checkCode(request.getParameter("captcha"), captcha);
            return super.attemptAuthentication(request, response);
        }
    }

    // 检查验证码
    public void checkCode(String requestCaptcha, String captcha) {
        if (StringUtils.isEmpty(requestCaptcha) || StringUtils.isEmpty(captcha) || !requestCaptcha.equalsIgnoreCase(captcha)) {
            //验证码不正确
            throw new AuthenticationServiceException("[LoginFilter] 验证码不正确");
        }
    }
}
