package dev.yxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;

import javax.sql.DataSource;

/**
 * Created by atom on 2021/2/2
 */
@Configuration
public class TokenRepositoryConfig {

    /**
     * remember-me持久化令牌，需要自己先建一个表
     * 见文件TokenRepository.txt
     */
    @Bean
    JdbcTokenRepositoryImpl jdbcTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }
}
