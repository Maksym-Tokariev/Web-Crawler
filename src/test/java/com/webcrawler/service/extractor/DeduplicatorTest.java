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

import static org.mockito.Mockito.*;

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
    void testAddUrl() {
        String url = "http://example.com/page";

        when(valueOps.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.just(true));

        StepVerifier.create(deduplicator.addUrlToDeduplication(url))
                .verifyComplete();

        verify(valueOps, times(1)).set(eq("url:http://example.com/page"), eq(url), any(Duration.class));
    }

    @Test
    void testHasUrlTrue() {
        String url = "http://example.com/page";

        when(redisTemplate.hasKey(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(deduplicator.hasUrl(url))
                .expectNext(true)
                .verifyComplete();

        verify(redisTemplate, times(1)).hasKey("url:http://example.com/page");
    }

    @Test
    void testHasUrlFalse() {
        String url = "http://example.com/page";

        when(redisTemplate.hasKey(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(deduplicator.hasUrl(url))
                .expectNext(false)
                .verifyComplete();

        verify(redisTemplate, times(1)).hasKey("url:http://example.com/page");
    }
}
