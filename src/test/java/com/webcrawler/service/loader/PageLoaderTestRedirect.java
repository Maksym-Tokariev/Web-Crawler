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
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

@SpringBootTest
public class PageLoaderTestRedirect {

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
    void testLoadPageRedirect() {
        String initialUrl = "/initial-page";
        String redirectUrl = "/redirect-page";
        String finalUrl = mockWebServer.url(redirectUrl).toString();
        String responseBody = "<html><body>Redirected Page</body></html>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(302)
                .setHeader(HttpHeaders.LOCATION, finalUrl)
        );

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .setBody(responseBody)
        );

        String fullUrl = mockWebServer.url(initialUrl).toString();

        StepVerifier.create(pageLoader.loadPageWithDelay(fullUrl, 0))
                .expectNextMatches(pageContent -> {
                    assert pageContent.getUrl().equals(finalUrl);
                    assert pageContent.getHtmlContent().equals(responseBody);
                    assert pageContent.getHeaders().getContentType().equals(MediaType.TEXT_HTML);
                    assert pageContent.getStatusCode().equals(HttpStatus.OK);
                    return true;
                })
                .verifyComplete();
    }

}
