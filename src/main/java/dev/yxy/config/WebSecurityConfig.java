package dev.yxy.config;

import dev.yxy.filter.CaptchaFilter;
import dev.yxy.filter.LoginFilter;
import dev.yxy.handler.CustomizeAuthenticationFailureHandler;
import dev.yxy.handler.CustomizeAuthenticationSuccessHandler;
import dev.yxy.handler.CustomizeEntryPoint;
import dev.yxy.handler.CustomizeLogoutSuccessHandler;
import dev.yxy.service.UserService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.security.Principal;

/**
 * Created by Nuclear on 2021/1/26
 */
@EnableWebSecurity//开启spring security
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true, proxyTargetClass = true)
//开启方法注解
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CaptchaFilter captchaFilter;

    @Autowired
    private UserService userService;

    /**
     * 加密类
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        // 测试可以用下面这个对前端传过来的密码不加密的PasswordEncoder
        // NoOpPasswordEncoder.getInstance()
        return new BCryptPasswordEncoder();
    }

    /**
     * 定义角色层次结构，即上级自动拥有下级的权限
     * 但是 只针对 prePost注解 是有效的，其他两种注解是无效的
     */
    @Bean
    RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER > ROLE_VISITOR");
        return hierarchy;
    }

    /**
     * 配置认证管理器
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 内存用户
        //auth.inMemoryAuthentication().withUser("jack").password("123456").roles("ADMIN")
        //        .and().withUser("rose").password("123456").roles("USER")
        //        .and().withUser("tony").password("123456").roles("VISITOR");

        auth.userDetailsService(userService);
    }

    /**
     * 配置忽略的路径
     * 请注意：
     * 这里的忽略是真正的忽略，不只是放行
     * 假如将Controller里的映射路径配置到这里，请求会被直接无视
     * 只有作为静态资源的第一次请求才会被响应
     * 参考验证码前端生成的图片的src
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // Spring Security should completely ignore URLs starting with /external/
        // 通过访问 /external/index.html 来进行测试
        web.ignoring().antMatchers("/external/**", "/js/**", "/favicon.ico");
    }

    /**
     * -----
     * defaultSuccessUrl("/") 重定向
     * successForwardUrl("/") 转发
     * 区别：
     * successForwardUrl是内部强制转发，不会记忆认证前的请求路径，比如你在/Hello路径下被拦截需要认证，认证后路径跳转到/login，页面显示/的内容
     * defaultSuccessUrl看源码可以发现，会记忆认证前的请求路径，比如你在/Hello路径下被拦截需要认证，认证后路径跳转到/Hello，页面显示/的内容
     * 当然，如果采用defaultSuccessUrl("/", true)这个方法，效果和successForwardUrl基本上一致
     * -----
     * <p>
     * 配置路径认证
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //todo 路径匹配规则
        http.authorizeRequests()
                .mvcMatchers("/auth").permitAll()//比较粗糙的匹配，可以放行所有/auth开头的路径，ant匹配即使写了/auth?code也没法放行/auth?code
                .antMatchers("/captcha", "/mobile/failure").permitAll()//这个一定要放行，不然会无限跳转
                .antMatchers(HttpMethod.GET, "/channel/anon").permitAll()//@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
                .antMatchers(HttpMethod.GET, "/channel/admin").hasRole("ADMIN")//sec:authorize-url="/channel/admin"
                .antMatchers(HttpMethod.POST, "/channel/user").hasRole("USER")//sec:authorize-url="POST /channel/user"
                .anyRequest().authenticated();//任何请求路径都需要认证

        //todo 处理验证码的两种方式
        //branchFrontFilter(http);
        branchReplaceFilter(http);

        //todo 配置登出逻辑
        http.logout()//登出
                //.logoutUrl("/logout")//登出路径，需要POST请求
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST", false))//与logoutUrl功能一致，可以修改请求方式
                //.logoutSuccessUrl("/auth")//登出成功后默认跳转的路径
                .logoutSuccessHandler(new CustomizeLogoutSuccessHandler("/auth"))//自定义登出成功处理器 与logoutSuccessUrl("/auth")选一个就行
                .deleteCookies("CAPTCHA")//可以配置登出时想要清除的Cookie
                .clearAuthentication(true)//清除认证信息，默认就是true
                .invalidateHttpSession(true);//使Session无效化，默认就是true

        //todo 配置未认证处理方案
        http.exceptionHandling().authenticationEntryPoint(new CustomizeEntryPoint("/auth"));//配置了这个 loginPage("/auth") 其实可以去掉，不过放着也没事 会被覆盖

        //todo 关闭跨域请求 不然登不上
        http.csrf().disable();
    }

    // 解决登录时验证码，方法一：加入前置过滤器
    public void branchFrontFilter(HttpSecurity http) throws Exception {
        //todo 加入前置过滤器 主要用来校验验证码
        http.addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class);

        //todo 配置登录逻辑
        http.formLogin()//表单登录
                .loginPage("/auth")//登录页面 见dev.yxy.controller.HelloController.login()
                .loginProcessingUrl("/login")//登录表单的action地址 可以自定义
                //.defaultSuccessUrl("/")//登录成功后默认跳转的路径 见dev.yxy.controller.HelloController.index()
                .successHandler(new CustomizeAuthenticationSuccessHandler("/"))//自定义认证成功处理器 与defaultSuccessUrl("/")选一个就行
                //.failureUrl("/auth?error=true")//登录失败后默认跳转的路径 见dev.yxy.controller.HelloController.login()
                .failureHandler(new CustomizeAuthenticationFailureHandler("/auth?error=true"))//自定义认证失败处理器 与failureUrl("/auth?error=true")选一个就行
                .usernameParameter("username")//可以自定义参数名称
                .passwordParameter("password")//可以自定义参数名称
                .permitAll();//以上路径全放行
    }

    // 解决登录时验证码，方法二：直接替换用户名密码认证过滤器，在自己重新的过滤器中处理验证码
    // 这种重写过滤器的方法自由度非常大，甚至可以换成json方式登录
    public void branchReplaceFilter(HttpSecurity http) throws Exception {
        http.addFilterAt(new LoginFilter(authenticationManagerBean()), UsernamePasswordAuthenticationFilter.class);
    }

    // 手动判断用户权限
    public static boolean isAdmin(@Nullable Principal principal) {
        if (principal == null) {
            return false;
        } else {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
            return token.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
    }
}
