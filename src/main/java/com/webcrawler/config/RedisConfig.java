package com.webcrawler.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Data
public class RedisConfig {

//    @Value("${spring.data.redis.jedis.pool.max-active}")
//    private int MAX_ACTIVE;
//
//    @Value("${spring.data.redis.jedis.pool.max-idle}")
//    private int MAX_IDLE;
//
//    @Value("${spring.data.redis.jedis.pool.min-idle}")
//    private int MIN_IDLE;
//
//    @Value("${spring.data.redis.jedis.pool.max-wait}")
//    private int TIMEOUT;
//
//    @Value("${spring.data.redis.host}")
//    private String HOST;
//
//    @Value("${spring.data.redis.port}")
//    private int PORT;

//    @Bean
//    public JedisPool jedisPool() {
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        jedisPoolConfig.setMaxTotal(MAX_ACTIVE);
//        jedisPoolConfig.setMaxIdle(MAX_IDLE);
//        jedisPoolConfig.setMinIdle(MIN_IDLE);
//
//        jedisPoolConfig.setJmxEnabled(false);
//
//        return new JedisPool(jedisPoolConfig, HOST, PORT, TIMEOUT);
//    }

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, String> redisSerializationContext = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .value(new StringRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(factory, redisSerializationContext);
    }
}
