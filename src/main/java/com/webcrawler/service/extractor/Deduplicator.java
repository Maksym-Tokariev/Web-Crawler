package com.webcrawler.service.extractor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;


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

    public Mono<Void> addUrlToDeduplication(String url) {
        String key = "url:" + url;
        log.debug("Attempting to add key: {}, value: {} to Redis with TTL: {}", key, url, TTL);
        return redisTemplate.opsForValue()
                .setIfAbsent(key, url, Duration.ofSeconds(TTL))
                .doOnError(e ->
                        log.error("Error while adding url: {} to deduplicator: {}", url, e.getMessage(), e)
                )
                .doOnSuccess(success ->
                        log.info("Adding url {} to the deduplicator", url)
                )
                .doOnTerminate(() ->
                        log.debug("Operation for URL: {} terminated", url)
                ).then();
    }

    public Mono<Boolean> hasUrl(String url) {
        String key = "url:" + url;
        return redisTemplate.hasKey(key);
    }
}
