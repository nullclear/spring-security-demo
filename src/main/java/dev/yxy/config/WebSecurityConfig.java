package dev.yxy.config;

import dev.yxy.controller.KaptchaController;
import dev.yxy.details.CustomizeWebAuthenticationDetails;
import dev.yxy.filter.CaptchaFilter;
import dev.yxy.filter.LoginFilter;
import dev.yxy.handler.CustomizeAuthenticationFailureHandler;
import dev.yxy.handler.CustomizeAuthenticationSuccessHandler;
import dev.yxy.handler.CustomizeEntryPoint;
import dev.yxy.handler.CustomizeLogoutSuccessHandler;
import dev.yxy.model.Member;
import dev.yxy.provider.CustomizeAuthenticationProvider;
import dev.yxy.service.UserService;
import dev.yxy.strategy.CustomizeSessionInformationExpiredStrategy;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
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
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.session.web.http.SessionEventHttpSessionListenerAdapter;

import java.security.Principal;

/**
 * RedisIndexedSessionRepository 的bean 来自 {@link RedisHttpSessionConfiguration}
 * {@link SecurityContextPersistenceFilter} 这个过滤器用于readSecurityContextFromSession，至于spring session怎么从数据库里来，哪是另外的事
 * {@link SecurityContextPersistenceFilter} 会优先于{@link UsernamePasswordAuthenticationFilter}执行
 * ----
 * {@link AbstractAuthenticationFilterConfigurer}#configure() 配置了一些未自定义的属性
 * ----
 * 先装载完Bean才调用配置方法?(大概)，有些没必要写成Bean
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

    @Autowired
    private RedisIndexedSessionRepository redisIndexedSessionRepository;

    @Autowired
    private JdbcTokenRepositoryImpl jdbcTokenRepository;

    /**
     * 假如下面不配置passwordEncoder(passwordEncoder())，这个配置为Bean也能有效
     * 具体注入到哪儿了，暂时不知道，spring源码太复杂了
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
        // todo 内存用户
        //auth.inMemoryAuthentication().withUser("jack").password("123456").roles("ADMIN")
        //        .and().withUser("rose").password("123456").roles("USER")
        //        .and().withUser("tony").password("123456").roles("VISITOR");

        // todo 这个调用默认的DaoAuthenticationProvider
        // todo 这个不能去掉，因为如果使用remember-me的话，这个是必要的，remember-me的userDetailsService来自 WebSecurityConfigurerAdapter 的内部类 UserDetailsServiceDelegator
        // todo 这个配置的相当于全局的userDetailsService，哪里缺少都会来这里拿
        auth.userDetailsService(userService);

        // todo 自定义Provider 可以增强验证逻辑
        CustomizeAuthenticationProvider provider = new CustomizeAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        auth.authenticationProvider(provider);
    }

    /**
     * 配置忽略的路径
     * todo 请注意：
     * 这里的忽略是真正的忽略，不只是放行
     * 假如将Controller里的映射路径配置到这里，请求会被直接无视
     * 只有作为静态资源的第一次请求才会被响应
     * 参考验证码前端生成的图片的src
     * -----
     * 经过查证，是浏览器缓存的原因，解决办法见{@link KaptchaController}里的两个方法
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // Spring Security should completely ignore URLs starting with /external/
        // 通过访问 /external/index.html 来进行测试
        web.ignoring().antMatchers("/external/**", "/js/**", "/favicon.ico", "/no-cache", "/random");
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
     * 如果配置了.tokenRepository(jdbcTokenRepository) 就使用 {@link PersistentTokenBasedRememberMeServices}
     * 没有配置就使用 {@link TokenBasedRememberMeServices}
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
        branchFrontFilter(http);
        //branchReplaceFilter(http);

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

        //todo 记住我
        http.rememberMe().key("spring-security-demo")
                //.userDetailsService(userService) //remember-me是需要使用loadUserByUsername()这个方法的，这里不配置就使用上面的配置，这里配置了就顶替上面的配置
                //说到这个，remember-me里面还能配置authenticationSuccessHandler，logoutHandler
                .tokenValiditySeconds(AbstractRememberMeServices.TWO_WEEKS_S)//客户端token过期时间
                .tokenRepository(jdbcTokenRepository);//remember-me的token仓库，可以存在内存，也可以存在数据库

        // todo 限制登录
        springSessionManagement(http);
    }

    // 解决登录时验证码，方法一：加入前置过滤器
    public void branchFrontFilter(HttpSecurity http) throws Exception {
        //todo 加入前置过滤器 主要用来校验验证码
        http.addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class);

        //todo 配置登录逻辑
        http.formLogin()//表单登录
                .authenticationDetailsSource(CustomizeWebAuthenticationDetails::new)// 自定义认证details的源
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

    // todo 警告，如果自己替换认证过滤器，会有些功能丢失，需要自己去实现，具体怎么做需要详细参考源码，非常麻烦，比如remember-me功能就会丢失
    // 解决登录时验证码，方法二：直接替换用户名密码认证过滤器，在自己重新的过滤器中处理验证码
    // 这种重写过滤器的方法自由度非常大，甚至可以换成json方式登录
    public void branchReplaceFilter(HttpSecurity http) throws Exception {
        http.addFilterAt(new LoginFilter(authenticationManagerBean()), UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Session管理器
     * -----
     * 何时调用？
     * 调用 {@link AbstractAuthenticationProcessingFilter}#doFilter() 方法中的sessionStrategy.onAuthentication(authResult, request, response);
     * 其实例就是 {@link ConcurrentSessionControlAuthenticationStrategy}#onAuthentication() 方法
     * -----
     * maxSessionsPreventsLogin(true)这么设置会有一个巨大的问题，假如用户没有注销，那就没人能登录上去了
     * -----
     * 默认的session过期策略是 {@link ConcurrentSessionFilter}#ResponseBodySessionInformationExpiredStrategy
     * 直接输出的This session has been expired (possibly due to multiple concurrent logins being attempted as the same user).就来源于此
     * -----
     * 如果自定义了expiredUrl("/auth?max-session=true")，session过期策略就变了，其实例在{@link SimpleRedirectSessionInformationExpiredStrategy}
     */
    public void sessionManagement(HttpSecurity http) throws Exception {
        http.sessionManagement().maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/auth?max-session=true");
    }

    /**
     * Session管理器，这个用于spring-session
     * -----
     * 默认的sessionRegistry是 {@link SessionRegistryImpl} ，如果需要使用spring-session，需要另外设置
     * {@link SessionRegistryImpl} 有一个坑，里面的Map是用Principal做key的，所以我们的 {@link Member} 需要实现equals和hashcode
     * -----
     * 可以自定义session过期策略
     * -----
     * 可以使用.sessionAuthenticationStrategy()自定义session认证策略，参考{@link ConcurrentSessionControlAuthenticationStrategy}
     * 比如我设置禁止后续登录，但是我前置的Session又没有注销，那关闭窗口后就没人能登上了，自定义也是可以解决的
     */
    public void springSessionManagement(HttpSecurity http) throws Exception {
        http.sessionManagement().maximumSessions(2)
                .sessionRegistry(new SpringSessionBackedSessionRegistry<>(redisIndexedSessionRepository))
                .maxSessionsPreventsLogin(false)
                .expiredSessionStrategy(new CustomizeSessionInformationExpiredStrategy("/auth?max-session=true"));
    }

    /**
     * 这个Bean会被 {@link SessionEventHttpSessionListenerAdapter}#onApplicationEvent() 调用，到底怎么进去的，可能是由spring内部管理的
     * -----
     * 这个类实现了 HttpSessionListener 接口，在该 Bean 中，可以及时感知 session 创建和销毁的事件，
     * 并且调用 Spring 中的事件机制将相关的创建和销毁事件发布出去，进而被 Spring Security 感知到
     * -----
     * 不过即使没有bean，据我的测试也没有啥问题，不懂原因在哪
     * -----
     * 创建了这个Bean，还会出现 RedisConnectionFactory is required 的问题
     * {@link RedisConfig}#sessionRedisOperations 就是为了解决这个问题
     * 直接替换 {@link RedisIndexedSessionRepository} 里需要的 sessionRedisOperations
     * -----
     * 还有必须注意 {@link RedisIndexedSessionRepository} 里的defaultSerializer 是 {@link JdkSerializationRedisSerializer}
     * 这个关系着 {@link RedisIndexedSessionRepository}#onMessage() 里的反序列化
     * 所以{@link RedisConfig}#sessionRedisOperations 里的value序列化只能是 {@link JdkSerializationRedisSerializer}
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
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
