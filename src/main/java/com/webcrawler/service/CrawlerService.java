package com.webcrawler.service;

import com.webcrawler.service.loader.PageLoader;
import com.webcrawler.service.parser.HtmlParser;
import com.webcrawler.service.queue.UrlQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class CrawlerService {

    @Value("${service.max.concurrency}")
    private int MAX_CONCURRENCY;

    @Value("${service.max.parse.count}")
    private int MAX_PARSE_COUNT;

    private final PageLoader pageLoader;

    private final HtmlParser parser;

    private final UrlQueueService urlQueueService;

    private final AtomicInteger parseCount = new AtomicInteger(0);

    public CrawlerService(PageLoader pageLoader, HtmlParser parser, UrlQueueService urlQueueService) {
        this.pageLoader = pageLoader;
        this.parser = parser;
        this.urlQueueService = urlQueueService;
    }

    public void startCrawling() {
        Flux.range(1, MAX_PARSE_COUNT)
                .flatMap(i -> {
                    if (!urlQueueService.isEmpty()) {
                        String url = urlQueueService.takeUrl();
                        return crawlUrl(url);
                    } else
                        return Flux.empty();
                }, MAX_CONCURRENCY)
                .doOnComplete(() -> log.debug("Crawling completed with {} URLs.", parseCount.get()))
                .subscribe();
    }

    private Mono<Void> crawlUrl(String url) {
        log.debug("Start crawling URL {}", url);
        return pageLoader.loadPageWithDelay(url, 5000)
                .filter(Objects::nonNull)
                .flatMap(pageContent -> {
                    parser.parse(pageContent.getHtmlContent(), url);
                    parseCount.incrementAndGet();
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                   log.error("Error while crawling URL {}", url, e);
                   return Mono.empty();
                }).then();
    }
}
