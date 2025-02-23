package com.webcrawler.service;

import com.webcrawler.service.loader.PageLoader;
import com.webcrawler.service.parser.HtmlParser;
import com.webcrawler.service.queue.UrlQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class CrawlerService {

    @Value("${service.max.concurrency}")
    public int MAX_CONCURRENCY;

    @Value("${service.max.parse.count}")
    public int MAX_PARSE_COUNT;

    private final PageLoader pageLoader;

    private final HtmlParser parser;

    private final UrlQueueService urlQueueService;

    private final AtomicInteger parseCount = new AtomicInteger(0);

    public CrawlerService(PageLoader pageLoader, HtmlParser parser, UrlQueueService urlQueueService) {
        this.pageLoader = pageLoader;
        this.parser = parser;
        this.urlQueueService = urlQueueService;
    }


    /**
     * Starts process of crawling.
     * Process will be run until parseCount reaches the MAX_PARSE_COUNT.
     */
    public Mono<Void> startCrawling() {
        return urlQueueService.consumeUrls()
                .takeWhile(i -> parseCount.get() < MAX_PARSE_COUNT)
                .flatMap(this::crawlUrl, MAX_CONCURRENCY)
                .doOnSubscribe(i -> log.info("Crawling started"))
                .doOnTerminate(() -> log.info("Crawling completed after {} URLs", parseCount.get()))
                .then();
    }

    /**
     * Handles loading and parsing URL.
     */
    public Mono<Void> crawlUrl(String url) {
        return pageLoader.loadPageWithDelay(url, 5000)
                .flatMap(pageContent -> parser.parse(pageContent.getHtmlContent(), url))
                .flatMap(urlQueueService::addUrls)
                .doOnSuccess(count -> parseCount.incrementAndGet())
                .onErrorResume(e -> {
                    log.error("Error crawling URL: {}", url, e);
                    return Mono.empty();
                }).then();
    }
}
