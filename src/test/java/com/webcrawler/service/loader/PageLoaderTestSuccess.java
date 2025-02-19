package com.webcrawler.service.loader;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

@SpringBootTest
public class PageLoaderTestSuccess {


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
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testLoadPageSuccess() {
        String testUrl = "/test-page";
        String fullUrl = mockWebServer.url(testUrl).toString();
        String responseBody = "<html><body>Test Page</body></html>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.TEXT_HTML_VALUE)
                .setBody(responseBody));

        StepVerifier.create(pageLoader.loadPage(fullUrl, 0))
                .expectNextMatches(pageContent -> {
                    assert pageContent.getUrl().equals(mockWebServer.url(testUrl).toString());
                    assert pageContent.getHtmlContent().equals(responseBody);
                    assert pageContent.getHeaders().getContentType().equals(org.springframework.http.MediaType.TEXT_HTML);
                    assert pageContent.getStatusCode().equals(HttpStatus.OK);
                    return true;
                })
                .verifyComplete();
    }
}
