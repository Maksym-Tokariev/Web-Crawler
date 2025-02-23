package com.webcrawler.service.parser;

import com.webcrawler.model.LinkInfo;
import com.webcrawler.service.extractor.Deduplicator;
import com.webcrawler.service.extractor.UrlExtractor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Parse HTML content and return Document object.
 */

@Slf4j
@Service
@Data
public class HtmlParser {

    private final UrlExtractor urlExtractor;
    private final Deduplicator deduplicator;

    /**
     * Parse HTML and return list of URL's.
     */
    public Mono<List<String>> parse(String htmlContent, String url) {
        return Mono.fromCallable(() -> Jsoup.parse(htmlContent, url))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(document -> processDocument(document, url));
    }

    /**
     * Handles the doc, marks the URL as visited and filters new URL's.
     */
    private Mono<List<String>> processDocument(Document document, String url) {
        return deduplicator.markAsVisited(url)
                .then(Mono.defer(() -> urlExtractor.extract(document)))
                .flatMap(links -> deduplicator.filterNewUrls(links.stream()
                        .map(LinkInfo::getUrl)
                        .collect(Collectors.toList())
                ));
    }
}
