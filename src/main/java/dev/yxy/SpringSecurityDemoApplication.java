package dev.yxy;

import dev.yxy.property.RedisProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.session.web.http.SessionRepositoryFilter;

/**
 * 过滤器顺序
 * {@link SessionRepositoryFilter}
 * {@link SecurityContextPersistenceFilter}
 * {@link UsernamePasswordAuthenticationFilter}
 */
@EnableConfigurationProperties(value = {RedisProperty.class})
@SpringBootApplication
public class SpringSecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityDemoApplication.class, args);
    }

}
