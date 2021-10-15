package dev.yxy.config;

import dev.yxy.property.RedisProperty;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.SpringSessionRedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.SessionRepositoryFilter;

/**
 * 自定义过滤器配置：{@link org.springframework.boot.autoconfigure.session.SessionRepositoryFilterConfiguration}
 * 库到过滤器：{@link RedisIndexedSessionRepository} -> {@link SpringHttpSessionConfiguration#springSessionRepositoryFilter(SessionRepository)}
 * -------------------------------------------------- Spring Session CRUD的来源 --------------------------------------------------
 * {@link SessionRepositoryFilter#doFilterInternal} 134行
 * {@link SessionRepositoryFilter.SessionRepositoryRequestWrapper#commitSession()} 225行
 * {@link SessionRepositoryFilter.SessionRepositoryRequestWrapper#getSession(boolean)} 318行
 * {@link SessionRepositoryFilter.SessionRepositoryRequestWrapper#getRequestedSession()} 351行
 * {@link SessionRepositoryFilter.SessionRepositoryRequestWrapper.HttpSessionWrapper#invalidate()} 387行
 * <p>
 * Spring Session的配置类
 */
@SuppressWarnings("JavadocReference")
@EnableRedisHttpSession//开启spring session
@Configuration
public class RedisHttpSessionConfig {

    @Autowired
    private RedisProperty redisProperty;

    // -------------------------------------------------- spring session 通过 redis 实现持久化 --------------------------------------------------

    /**
     * 替换源码里的连接工厂 {@link RedisHttpSessionConfiguration#setRedisConnectionFactory(ObjectProvider, ObjectProvider)}
     * 1、springSession使用的数据库连接工厂
     */
    @SpringSessionRedisConnectionFactory
    @Bean(name = "springSessionRedisConnectionFactory")
    public LettuceConnectionFactory springSessionRedisConnectionFactory() {
        return createLettuceConnectionFactory(redisProperty, redisProperty.getSessionDatabase());
    }

    /**
     * 2、spring-session持久化redis操作对象
     */
    // @Bean("sessionRedisOperations")
    public RedisOperations<Object, Object> sessionRedisOperations(LettuceConnectionFactory springSessionRedisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(springSessionRedisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.java());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.java());
        return redisTemplate;
    }

    /**
     * （非必要替换）
     * 默认的redis库来源：{@link RedisHttpSessionConfiguration#sessionRepository()}
     * 3、spring-session持久化之Redis索引会话存储库
     */
    // @Bean("redisIndexedSessionRepository")
    // @Primary
    public RedisIndexedSessionRepository redisIndexedSessionRepository(RedisOperations<Object, Object> sessionRedisOperations) {
        return new RedisIndexedSessionRepository(sessionRedisOperations);
    }

    // -------------------------------------------------- cookie相关设置 --------------------------------------------------

    /**
     * 注入到了 {@link SpringHttpSessionConfiguration}
     * 可以设置spring-session返回的Cookie的各种属性
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setCookieName("SPRING_SESSION");
        return defaultCookieSerializer;
    }

    // -------------------------------------------------- 内部方法 --------------------------------------------------

    /**
     * 创建lettuce连接工厂
     */
    protected static LettuceConnectionFactory createLettuceConnectionFactory(RedisProperty property, int database) {
        // redis Standalone配置
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName(property.getHost());
        standaloneConfig.setPort(property.getPort());
        standaloneConfig.setPassword(property.getPassword());
        standaloneConfig.setDatabase(database);

        // 通用连接池配置
        GenericObjectPoolConfig<RedisHttpSessionConfig> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(property.getLettuce().getPool().getMaxActive());
        poolConfig.setMaxIdle(property.getLettuce().getPool().getMaxIdle());
        poolConfig.setMinIdle(property.getLettuce().getPool().getMinIdle());
        poolConfig.setMaxWaitMillis(property.getLettuce().getPool().getMaxWait().toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(property.getLettuce().getPool().getTimeBetweenEvictionRuns().toMillis());

        // lettuce客户端连接池配置
        LettucePoolingClientConfiguration lettuceConfig = LettucePoolingClientConfiguration.builder()
                .clientName(property.getClientName())
                .commandTimeout(property.getTimeout())
                .shutdownTimeout(property.getLettuce().getShutdownTimeout())
                .poolConfig(poolConfig)
                .build();

        return new LettuceConnectionFactory(standaloneConfig, lettuceConfig);
    }
}
