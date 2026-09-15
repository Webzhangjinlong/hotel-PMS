package com.hotel.pms.dao.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * <p>
 * 配置RedisTemplate和Redisson分布式锁客户端
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.data.redis.password:}")
    private String redisPassword;
    
    /**
     * 配置RedisTemplate
     * <p>
     * 设置序列化器，支持JSON序列化
     * </p>
     * 
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 【Key序列化】使用String序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // 【Value序列化】使用JSON序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 配置Redisson客户端
     * <p>
     * 用于分布式锁
     * </p>
     * 
     * @return RedissonClient
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 【配置Redis地址】
        String address = "redis://" + redisHost + ":" + redisPort;
        config.useSingleServer().setAddress(address);
        
        // 【配置密码】如果有的话
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }
        
        // 【配置连接池】
        config.useSingleServer().setConnectionMinimumIdleSize(5);
        config.useSingleServer().setConnectionPoolSize(20);
        
        return Redisson.create(config);
    }
}