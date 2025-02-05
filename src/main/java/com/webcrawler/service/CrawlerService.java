package com.webcrawler.service;

import com.webcrawler.model.PageContent;
import com.webcrawler.service.extractor.UrlExtractor;
import com.webcrawler.service.loader.PageLoader;
import com.webcrawler.service.parser.HtmlParser;
import com.webcrawler.service.queue.UrlQueueService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
@Data
public class CrawlerService {

    private final PageLoader pageLoader;
    private final UrlExtractor extractor;
    private final HtmlParser parser;
    private final UrlQueueService urlQueueService;

    public void startCrawling(String url) {
//        while (true) {
            log.info("Crawling url: {}", url);
            Mono<PageContent> contentMono = pageLoader.loadPageWithDelay(url, 5000);
            PageContent pageContent = contentMono.block();

            if (pageContent != null) {
                String urlItem = pageContent.getUrl();
                try {
                    Document document = parser.parse(pageContent.getHtmlContent(), urlItem);
                    extractor.extract(document);
                } catch (Exception e) {
                    log.error("Error while crawling url: {}", urlItem, e);
                }
//            }

//            log.info("Crawling url: {}", url);
//            Optional<UrlQueueService> optionalItem = urlQueueService.getNextUrl();
//
//            if (optionalItem.isPresent()) {
//                UrlQueueService item = optionalItem.get();
//                String urlItem = item.getUrl();
//
//                try {
//                    Mono<PageContent> pageContentMono = pageLoader.loadPageWithDelay(urlItem, 5000);
//                    PageContent pageContent = pageContentMono.block();
//
//                    if (pageContent != null) {
//                        Document document = parser.parse(pageContent.getHtmlContent(), urlItem);
//
//                        extractor.extract(document);
//                    }
//                } catch (Exception e) {
//                    log.error("Error while crawling url: {}", urlItem, e);
//                }
//            } else {
//                log.info("The queue is empty. Stop crawling process.");
//                break;
//            }
        }
    }
}
