package com.webcrawler.service;

import com.webcrawler.service.loader.PageLoader;
import com.webcrawler.service.parser.HtmlParser;
import com.webcrawler.service.queue.UrlQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CrawlerService {

    private final PageLoader pageLoader;
    private final HtmlParser parser;
    private UrlQueueService urlQueueService;
    private final Deduplicator deduplicator;
    private static final int MAX_PARSE_COUNT = 100;
    private int parseCount = 0;

    public CrawlerService(PageLoader pageLoader, HtmlParser parser, Deduplicator deduplicator) {
        this.deduplicator = deduplicator;
        this.pageLoader = pageLoader;
        this.parser = parser;
    }

    @Autowired
    public void setUrlQueueService(UrlQueueService urlQueueService) {
        this.urlQueueService = urlQueueService;
        this.urlQueueService.setCrawlerService(this);
    }

    public void startCrawling(String url) {

        if (wasVisited(url)) {
            log.debug("An url: {} has already been visited is skipped.", url);
        } else {
            System.out.println("<----- [Parse count: " + parseCount + "] ------>");
            crawlUrl(url);
            parseCount++;
        }


    }

    public void crawlUrl(String url) {
        if (url != null && parseCount < MAX_PARSE_COUNT) {
            log.info("Crawling url: {}", url);
            pageLoader.loadPageWithDelay(url, 5000)
                    .flatMap(pageContent -> {
                        String urlItem = pageContent.getUrl();
                        try {
                            parser.parse(pageContent.getHtmlContent(), urlItem);

                        } catch (Exception e) {
                            log.error("Error while crawling url: {}", urlItem, e);
                        }
                        return Mono.empty();
                    })
                    .doOnError(e -> log.error("Error while crawling url: {}", url, e))
                    .subscribe();
        } else {
            log.info("Crawling was stopped because the number of parses has maxed out (100): {}", parseCount);
        }
    }

    public void startCrawling() {
        String startUrl = urlQueueService.pushUrl();
        startCrawling(startUrl);
    }

    public boolean wasVisited(String url) {
        return deduplicator.wasVisited(url);
    }
}
