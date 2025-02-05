package com.webcrawler.service;

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

    @Override
    public void run(String... args) throws Exception {
        log.info("--- Starting Crawler ---");
        String startUrl = "https://ru.wikipedia.org/wiki/%D0%A5%D0%B8%D0%BC%D0%B8%D1%87%D0%B5%D1%81%D0%BA%D0%B0%D1%8F_%D0%BA%D0%B8%D0%BD%D0%B5%D1%82%D0%B8%D0%BA%D0%B0";

        crawlerService.startCrawling(startUrl);
    }
}
