package com.webcrawler.service.loader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.Mockito.*;

public class RobotsTxtHandlerTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private RobotsTxtHandler robotsTxtHandler;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsAllowedTrue() throws MalformedURLException {
        String url = "http://example.com/page";
        URL urlObj = new URL(url);
        String host = urlObj.getHost();
        String robotsUrl = urlObj.getProtocol() + "://" + host + "/robots.txt";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(robotsUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(new byte[0]));

        StepVerifier.create(robotsTxtHandler.isAllowed(url))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testIsAllowedNotAccessible() throws MalformedURLException {
        String url = "http://example.com/page";
        URL urlObj = new URL(url);
        String host = urlObj.getHost();
        String robotsUrl = urlObj.getProtocol() + "://" + host + "/robots.txt";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(robotsUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(robotsTxtHandler.isAllowed(url))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testIsAllowedInvalidUrl() {
        String url = "Invalid-url";

        StepVerifier.create(robotsTxtHandler.isAllowed(url))
                .expectNext(true)
                .verifyComplete();

    }

    @Test
    void testIsAllowedDisallowed() throws MalformedURLException {
        String url = "http://example.com/disallowed-page";
        URL urlObj = new URL(url);
        String host = urlObj.getHost();
        String robotsUrl = urlObj.getProtocol() + "://" + host + "/robots.txt";
        byte[] robotsTxt = "User-agent: *\nDisallow: /disallowed-page".getBytes();

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(robotsUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(robotsTxt));

        StepVerifier.create(robotsTxtHandler.isAllowed(url))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testIsAllowedCacheRobotsTxt() throws MalformedURLException {
        String url = "http://example.com/disallowed-page";
        URL urlObj = new URL(url);
        String host = urlObj.getHost();
        String robotsUrl = urlObj.getProtocol() + "://" + host + "/robots.txt";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(robotsUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(new byte[0]));

        StepVerifier.create(robotsTxtHandler.isAllowed(url))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(robotsTxtHandler.isAllowed(url))
                .expectNext(true)
                .verifyComplete();

        verify(webClient, times(1)).get();
    }
}
