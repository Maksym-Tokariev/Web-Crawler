package com.webcrawler.service.extractor;

import com.webcrawler.model.LinkInfo;
import com.webcrawler.service.DatabaseService;
import com.webcrawler.service.queue.UrlQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
   Extract links from a page and adds them to a queue and DB.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlExtractor {

    private final UrlQueueService urlQueueService;
    private final Deduplicator deduplicator;
    private final KeywordExtractor keywordExtractor;
    private final DatabaseService databaseService;


    /**
     * Starts the process of extracting urls and keywords from a document and returns a LinkInfo class.
     */
    public Mono<List<LinkInfo>> extract(Document document) {
        return Mono.fromCallable(() -> {
                    log.info("Extracting urls from document: {}", document.title());
                    return document.select("a[href]");
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::processElement)
                .collectList()
                .flatMap(this::processUrls);
    }

    /**
     * Extract keyword from a link and create a LinkInfo class with the url and keywords.
     */
    private Mono<LinkInfo> processElement(Element link) {
        return Mono.fromCallable(() -> link.attr("abs:href"))
                .filter(this::isValidUrl)
                .flatMap(url ->
                        keywordExtractor.extractKeywords(getLinkText(link))
                                .map(keywords -> new LinkInfo(url, keywords))
                );
    }

    /**
     * Checks whether the URL has been visited,
     * if not, then adds it's along with the text to the DB and queue.
     */
    public Mono<List<LinkInfo>> processUrls(List<LinkInfo> extractedUrls) {
        log.trace("processing urls from list: {}", extractedUrls);
        return Flux.fromIterable(extractedUrls)
                .filter(linkInfo -> linkInfo.getUrl() != null)
                .filterWhen(linkInfo -> deduplicator.isNotProcessed(linkInfo.getUrl()))
                .collectList()
                .flatMap(links -> {
                    List<String> urls = links.stream()
                            .map(LinkInfo::getUrl)
                            .toList();

                    return databaseService.saveAllLinkInfo(links)
                            .then(urlQueueService.addUrls(urls))
                            .thenReturn(links);

                });
    }


    /**
     * Extract text, title and alt from link.
     */
    public String getLinkText(Element link) {
        log.debug("Getting link text from: {}", link.attr("abs:href"));
        return Stream.of(
                        link.text().trim(),
                        link.attr("title").trim(),
                        link.attr("alt").trim()
                ).filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
    }


    private boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null
                    && uri.getHost() != null
                    && !uri.getPath().matches(".*\\.(jpg|png|gif|pdf|zip|rar|exe)$");
        } catch (URISyntaxException e) {
            log.debug("Invalid URL syntax: {}", url);
            return false;
        }
    }

}
