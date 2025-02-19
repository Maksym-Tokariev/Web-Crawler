package com.webcrawler.service;


import com.webcrawler.service.loader.PageLoader;
import com.webcrawler.service.loader.RobotsTxtHandler;
import com.webcrawler.service.parser.HtmlParser;
import com.webcrawler.service.queue.UrlQueueService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CrawlerServiceTest {

    @Mock
    private PageLoader pageLoader;

    @Mock
    private HtmlParser htmlParser;

    @Mock
    private UrlQueueService urlQueueService;

    @Mock
    private RobotsTxtHandler robotsTxtHandler;

    @InjectMocks
    private CrawlerService crawlerService;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        pageLoader = new PageLoader(webClient, new RobotsTxtHandler());

        crawlerService = new CrawlerService(pageLoader, htmlParser, urlQueueService);

        ReflectionTestUtils.setField(crawlerService, "MAX_CONCURRENCY", 2);
        ReflectionTestUtils.setField(crawlerService, "MAX_PARSE_COUNT", 5);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testCrawlUrlSuccess() {
        String testUrl = "/test-page";
        String fullUrl = mockWebServer.url(testUrl).toString();
        String respBody = "<html><body>Test Page</body></html>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .setBody(respBody)
        );

        StepVerifier.create(crawlerService.crawlUrl(fullUrl))
                .expectSubscription()
                .expectError(RuntimeException.class)
                .verify();

        verify(htmlParser, times(1)).parse(respBody, fullUrl);
        verifyNoMoreInteractions(htmlParser, urlQueueService);
    }

    @Test
    void testCrawlUrlError() {
        String testUrl = "/test-page";
        String fullUrl = mockWebServer.url(testUrl).toString();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                .setBody("Internal Server Error"));

        StepVerifier.create(crawlerService.crawlUrl(fullUrl))
                .expectSubscription()
                .verifyComplete();

        verify(htmlParser, never()).parse(anyString(), anyString());
        verifyNoMoreInteractions(htmlParser, urlQueueService);
    }
}
