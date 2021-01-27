package dev.yxy.config;

import dev.yxy.service.UserService;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
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

import java.security.Principal;

/**
 * Created by Nuclear on 2021/1/26
 */
@EnableWebSecurity//开启spring security
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true, proxyTargetClass = true)
//开启方法注解
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Bean
    PasswordEncoder passwordEncoder() {
        // 测试可以用下面这个对前端传过来的密码不加密的PasswordEncoder
        // NoOpPasswordEncoder.getInstance()
        return new BCryptPasswordEncoder();
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
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // Spring Security should completely ignore URLs starting with /external/
        // 通过访问 /external/index.html 来进行测试
        web.ignoring().antMatchers("/external/**", "/js/**");
    }

    /**
     * 配置路径认证
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/channel/anon").permitAll()//@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
                .antMatchers(HttpMethod.GET, "/channel/admin").hasRole("ADMIN")//sec:authorize-url="/channel/admin"
                .antMatchers(HttpMethod.POST, "/channel/user").hasRole("USER")//sec:authorize-url="POST /channel/user"
                .anyRequest().authenticated()//任何请求路径都需要认证
                .and()
                .formLogin()//表单登录
                .loginPage("/auth")//登录页面 见dev.yxy.controller.HelloController.login()
                .loginProcessingUrl("/login")//登录表单的action地址 可以自定义
                .defaultSuccessUrl("/")//登录成功后默认跳转的路径 见dev.yxy.controller.HelloController.index()
                .failureUrl("/auth?error=true")//登录失败后默认跳转的路径 见dev.yxy.controller.HelloController.login()
                .permitAll()//以上路径全放行
                .and()
                .logout()//登出
                .logoutUrl("/logout")//登出路径，需要POST请求
                .logoutSuccessUrl("/auth")//登出成功后默认跳转的路径
                .and()
                .csrf().disable();//关闭跨域请求 不然登不上
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
