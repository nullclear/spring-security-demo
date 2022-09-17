package dev.yxy;

import dev.yxy.property.RedisProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.session.web.http.SessionRepositoryFilter;

/**
 * 过滤器顺序
 * {@link SessionRepositoryFilter}
 * {@link SecurityContextPersistenceFilter}
 * {@link UsernamePasswordAuthenticationFilter}
 *
 * NOTE - 2022/09/17 责任链模式
 * Spring Security 内部的虚拟过滤器链 {@link FilterChainProxy.VirtualFilterChain}
 * 在原始过滤器链中建立一个虚拟过滤器链，执行完毕继续执行原始过滤器链
 */
@SuppressWarnings("JavadocReference")
@EnableConfigurationProperties(value = {RedisProperty.class})
@SpringBootApplication
public class SpringSecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityDemoApplication.class, args);
    }

}
