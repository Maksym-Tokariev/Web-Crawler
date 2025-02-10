package com.webcrawler.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Data
public class RedisConfig {

    @Value("${spring.data.redis.jedis.pool.max-active}")
    private int MAX_ACTIVE;

    @Value("${spring.data.redis.jedis.pool.max-idle}")
    private int MAX_IDLE;

    @Value("${spring.data.redis.jedis.pool.min-idle}")
    private int MIN_IDLE;

    @Value("${spring.data.redis.jedis.pool.max-wait}")
    private int TIMEOUT;

    @Value("${spring.data.redis.host}")
    private String HOST;

    @Value("${spring.data.redis.port}")
    private int PORT;

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(MAX_ACTIVE);
        jedisPoolConfig.setMaxIdle(MAX_IDLE);
        jedisPoolConfig.setMinIdle(MIN_IDLE);

        jedisPoolConfig.setJmxEnabled(false);

        return new JedisPool(jedisPoolConfig, HOST, PORT, TIMEOUT);
    }
}
