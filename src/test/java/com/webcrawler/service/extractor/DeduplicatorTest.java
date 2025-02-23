package com.webcrawler.service.extractor;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

public class DeduplicatorTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, Object> valueOps;

    @InjectMocks
    private Deduplicator deduplicator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void testMarkAsVisited() {
        String url = "http://example.com/page";
        String key = "url:" + url;

        when(valueOps.setIfAbsent(eq(key), eq(url), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Boolean> res = deduplicator.markAsVisited(url);

        StepVerifier.create(res)
                .expectNext(true)
                .verifyComplete();

        verify(valueOps, times(1)).setIfAbsent(eq(key), eq(url), any(Duration.class));
    }

    @Test
    void testIsNotProcessed_shouldReturnTrueWhenKeyDoesNotExist() {
        String url = "http://example.com/page";
        String key = "url:" + url;

        when(redisTemplate.hasKey(key)).thenReturn(Mono.just(false));

        Mono<Boolean> res = deduplicator.isNotProcessed(url);

        StepVerifier.create(res)
                .expectNext(true)
                .verifyComplete();

        verify(redisTemplate, times(1)).hasKey(key);
    }

    @Test
    void testIsNotProcessed_shouldReturnFalseWhenKeyExists() {
        String url = "http://example.com/page";
        String key = "url:" + url;

        when(redisTemplate.hasKey(key)).thenReturn(Mono.just(true));

        Mono<Boolean> res = deduplicator.isNotProcessed(url);

        StepVerifier.create(res)
                .expectNext(false)
                .verifyComplete();

        verify(redisTemplate, times(1)).hasKey(key);
    }

    @Test
    void testFilterNewUrls() {
        List<String> urls = Arrays.asList("http://example.com/page1", "http://example.com/page2", "http://example.com/page3");

        given(redisTemplate.hasKey(anyString())).willReturn(Mono.just(false));

        Mono<List<String>> res = deduplicator.filterNewUrls(urls);

        StepVerifier.create(res)
                .expectNext(urls)
                .verifyComplete();
    }
}
