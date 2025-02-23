package com.webcrawler.service;

import com.webcrawler.service.queue.UrlQueueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
  Entry point to crawler.
*/

@Slf4j
@Component
@AllArgsConstructor
public class CrawlerRunner implements CommandLineRunner {

    private final CrawlerService crawlerService;
    private final UrlQueueService urlQueueService;

    @Override
    public void run(String... args) {
        String startUrl = "https://www.pravda.com.ua/rus/news/";

        urlQueueService.addUrl(startUrl)
                .delayElement(Duration.ofSeconds(1))
                .then(crawlerService.startCrawling())
                .subscribe(
                        null,
                        e -> log.error("Crawler failed: {}", e.getMessage(), e),
                        () -> log.info("Crawler finished")
                );
    }
}
