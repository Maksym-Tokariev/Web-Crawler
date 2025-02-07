package com.webcrawler.service;

import com.webcrawler.service.queue.UrlQueueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/*
* Entry point to crawler.
*/

@Slf4j
@Component
@AllArgsConstructor
public class CrawlerRunner implements CommandLineRunner {

    private final CrawlerService crawlerService;
    private final UrlQueueService urlQueueService;

    @Override
    public void run(String... args) {
        log.info("--- Starting Crawler ---");
        String startUrl = "https://sudoku.org.ua/rus/";

        urlQueueService.addUrl(startUrl);
        crawlerService.startCrawling();
    }
}
