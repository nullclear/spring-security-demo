package dev.yxy.config;

import dev.yxy.property.RedisProperty;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.session.data.redis.config.annotation.SpringSessionRedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession//开启spring session
public class RedisConfig {

    @Autowired
    private RedisProperty redis;

    //lettuce连接工厂
    private LettuceConnectionFactory createLettuceConnectionFactory(int database) {
        //redis Standalone配置
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(redis.getHost(), redis.getPort());
        standaloneConfig.setDatabase(database);
        standaloneConfig.setPassword(redis.getPassword());

        //连接池配置
        GenericObjectPoolConfig<RedisConfig> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(redis.getLettuce().getPool().getMaxIdle());
        poolConfig.setMinIdle(redis.getLettuce().getPool().getMinIdle());
        poolConfig.setMaxTotal(redis.getLettuce().getPool().getMaxActive());
        poolConfig.setMaxWaitMillis(redis.getLettuce().getPool().getMaxWait().toMillis());

        //lettuce客户端连接池配置
        LettucePoolingClientConfiguration lettuceConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(redis.getTimeout())
                .shutdownTimeout(redis.getLettuce().getShutdownTimeout())
                .clientName(redis.getClientName())
                .poolConfig(poolConfig)
                .build();

        return new LettuceConnectionFactory(standaloneConfig, lettuceConfig);
    }

    //springSession使用的数据库连接工厂
    @SpringSessionRedisConnectionFactory
    @Bean(name = "springSessionRedisConnectionFactory")
    public LettuceConnectionFactory springSessionRedisConnectionFactory() {
        return createLettuceConnectionFactory(redis.getSessionDatabase());
    }
}
