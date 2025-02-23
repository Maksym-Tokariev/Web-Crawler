package com.webcrawler.service.extractor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;


/**
   A class to store url's that have already been visited.
 */

@Slf4j
@Service
public class Deduplicator {

    @Value("${spring.data.redis.ttl}")
    private int TTL;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Deduplicator(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> markAsVisited(String url) {
        String key = "url:" + url;
        log.debug("Attempting to add key: {}, value: {} to Redis with TTL: {}", key, url, TTL);
        return redisTemplate.opsForValue()
                .setIfAbsent(key, url, Duration.ofSeconds(TTL))
                .thenReturn(true);
    }

    public Mono<List<String>> filterNewUrls(List<String> urls) {
        log.debug("Attempting to filter new urls from Redis: {}", urls);
        return Flux.fromIterable(urls)
                .filterWhen(this::isNotProcessed)
                .collectList();
    }

    public Mono<Boolean> isNotProcessed(String url) {
        String key = "url:" + url;
        return redisTemplate.hasKey(key)
                .map(hasKey -> !hasKey);
    }
}
