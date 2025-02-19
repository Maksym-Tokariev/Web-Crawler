package com.webcrawler.service.loader;

import com.webcrawler.exceptions.ClientErrorException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

@SpringBootTest
public class PageLoaderTestClientError {
    @Autowired
    private WebClient.Builder webClientBuilder;

    private MockWebServer mockWebServer;

    private PageLoader pageLoader;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = webClientBuilder
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        RobotsTxtHandler robotsHandler = new  RobotsTxtHandler();

        pageLoader = new PageLoader(webClient, robotsHandler);
        ReflectionTestUtils.setField(pageLoader, "MAX_REDIRECTS", 3);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testLoadPageClientError() {
        String testUrl = "/test-client-error";
        String fullUrl = mockWebServer.url(testUrl).toString();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .setBody("Not Found")
        );

        StepVerifier.create(pageLoader.loadPage(fullUrl, 0))
                .expectErrorMatches(throwable -> {
                    boolean isClientErrorException = throwable instanceof ClientErrorException;
                    boolean containsErrorMessage = throwable.getMessage().contains("Client error: 404");

                    if (!isClientErrorException) {
                        System.out.println("Error type does not match: expected ClientErrorException but got " + throwable.getClass().getSimpleName());
                    }
                    if (!containsErrorMessage) {
                        System.out.println("Error message does not match: expected to contain 'Client error: 404' but got " + throwable.getMessage());
                    }

                    return isClientErrorException && containsErrorMessage;
                })
                .verify();
    }
}
