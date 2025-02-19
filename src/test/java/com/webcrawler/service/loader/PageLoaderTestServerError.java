package com.webcrawler.service.loader;

import com.webcrawler.exceptions.ServerErrorException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

@SpringBootTest
public class PageLoaderTestServerError {

    @Autowired
    private WebClient.Builder webClientBuilder;

    private MockWebServer mockWebServer;

    private PageLoader pageLoader;

    @BeforeEach
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = webClientBuilder
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        RobotsTxtHandler  robotsTxtHandler = new RobotsTxtHandler();

        pageLoader = new PageLoader(webClient, robotsTxtHandler);
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    void testLoadPageServerError() {
        String testUrl = "test-server-error";
        String fullUrl = mockWebServer.url(testUrl).toString();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .setBody("Internal Server Error")
        );

        StepVerifier.create(pageLoader.loadPageWithDelay(fullUrl, 0))
                .expectErrorMatches(e -> {
                    boolean isServerErrorException = e instanceof ServerErrorException;
                    boolean containsErrorMessage = e.getMessage().contains("Server error: 500");

                    if (!isServerErrorException) {
                        System.out.println("Error type does not match: expected ServerErrorException but got " + e.getClass().getSimpleName());
                    }
                    if (!containsErrorMessage) {
                        System.out.println("Error message does not match: expected to contain 'Server error: 500' but got " + e.getMessage());
                    }

                    return isServerErrorException && containsErrorMessage;
                })
                .verify();
    }

}
