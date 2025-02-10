package com.webcrawler.service.extractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 * A class to store url's that have already been visited
 */

@Slf4j
@Service
@Data
public class Deduplicator {

    @Value("${spring.data.redis.ttl}")
    private int TTL; // Temporal value
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    public void addUrlToDeduplication(String url) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "url: %s".formatted(url);

            jedis.setex(key, TTL, url);
            log.debug("Added to deduplicator URL: {}", url);
        } catch (Exception e) {
            log.error("Error while adding url: {} to deduplicator: {}", url, e.getMessage(), e);
        }
    }

    public boolean isNewUrl(String url) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "url: %s".formatted(url);
            boolean exists = jedis.exists(key);
            return !exists;
        } catch (Exception e) {
            log.error("Error while checking url: {} in deduplicator: {}", url, e.getMessage(), e);
            return false;
        }
    }
}
