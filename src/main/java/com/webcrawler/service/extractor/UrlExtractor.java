package com.webcrawler.service.extractor;

import com.webcrawler.model.LinkInfo;
import com.webcrawler.service.DatabaseService;
import com.webcrawler.service.queue.UrlQueueService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extract links from a page and adds them to a queue.
 */

@Slf4j
@Service
@Data
public class UrlExtractor {

    private final UrlQueueService urlQueueService;
    private final Deduplicator deduplicator;
    private final KeywordExtractor keywordExtractor;
    private final DatabaseService databaseService;

    public void extract(Document document) {
        log.info("Extracting urls from document: {}", document.title());

        Elements links = document.select("a[href]");
        Elements meta = document.select("meta[name=description, meta=keywords]"); //TODO

        List<LinkInfo> extractedUrls = links.stream()
                .map(this::extractLinkInfo)
                .filter(Objects::nonNull)
                .flatMap(Optional::stream)
                .toList();

        processUrls(extractedUrls)
                .doOnComplete(() -> log.info("Extracted and saved {} links with keywords", extractedUrls.size()))
                .subscribe(
                        null,
                        e -> log.error("Error while saving extracted links with keywords: {}", e.getMessage(), e)
                );
//            List<String> urls = extractedUrls.stream() --------------
//                    .map(LinkInfo::getUrl)
//                    .filter(Objects::nonNull)
//                    .filter(url -> {
//                        if (!deduplicator.isNewUrl(url)) {
//                            log.info("Skipping the duplicate url: {}", url);
//                            return false;
//                        }
//                        return true;
//                    })
//                    .collect(Collectors.toList());
//
//            List<String> urls = Flux.fromIterable(extractedUrls)
//                    .map(LinkInfo::getUrl)
//                    .filter(Objects::nonNull)
//                    .flatMap(url -> deduplicator.isNewUrl(url)
//                            .flatMap(isNew -> {
//                                if (!isNew) {
//                                    log.info("URL: {} is duplicate, skip", url);
//                                    return Mono.empty();
//                                } else {
//                                    log.info("URL: {} is new", url);
//                                    return Mono.just(url);
//                                }
//                            })
//                    )
//                    .collectList()
//                            .flatMapMany(urls)
//
//            urls.stream().iterator().forEachRemaining(System.out::println); // temp
//            urlQueueService.addUrls(urls);
//
//            extractedUrls.forEach(databaseService::saveLinkInfo);
//
//            Flux.fromIterable(extractedUrls)
//                    .flatMap(databaseService::saveLinkInfo)
//                    .doOnComplete(() -> log.info("Extracted and saved {} links with keywords", extractedUrls.size()))
//                    .subscribe();
    }

    private Flux<LinkInfo> processUrls(List<LinkInfo> extractedUrls) {
        return Flux.fromIterable(extractedUrls)
                .map(LinkInfo::getUrl)
                .filter(Objects::nonNull)
                .filterWhen(this::filterNewUrls)
                .collectList()
                .flatMapMany(urls -> {
                    logUrls(urls);
                    urlQueueService.addUrls(urls);
                    return saveLinkInfo(extractedUrls);
                });
    }

    private Flux<LinkInfo> saveLinkInfo(List<LinkInfo> extractedUrls) {
        return Flux.fromIterable(extractedUrls)
                .flatMap(databaseService::saveLinkInfo);
    }

    private void logUrls(List<String> urls) {
        urls.forEach(url -> log.debug("Processing URL: {}", url));
    }

    public Mono<Boolean> filterNewUrls(String url) {
        return deduplicator.isNewUrl(url)
                .flatMap(isNew -> {
                    if (!isNew) {
                        log.debug("URL: {} is duplicate, skip", url);
                        return Mono.just(true);
                    } else {
                        log.debug("URL: {} is new", url);
                        return Mono.just(false);
                    }
                });
    }

    private Optional<LinkInfo> extractLinkInfo(Element link) {
        String url = link.attr("abs:href");
        if (!isValidUrl(url)) {
            return Optional.empty();
        }

        String anchorText = link.text().trim();

        String title = link.attr("title").trim();
        String alt = link.attr("alt").trim();

        String text = Stream.of(anchorText, title, alt)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));

        Set<String> keywords = keywordExtractor.extractKeywords(text);
        if (!keywords.isEmpty()) {
            return Optional.of(new LinkInfo(url, keywords));
        } else {
            return Optional.empty();
        }
    }

    public boolean isValidUrl(String url) {
        if (!url.startsWith("http")) {
            return false;
        }
        String[] invalidUrls = {".jpg", ".png", ".gif", ".pdf", ".zip", ".rar", ".exe"};
        for (String ext : invalidUrls) {
            if (url.toLowerCase().endsWith(ext)) {
                log.debug("Invalid URL: {}. The URL must contain 'http/https' and shouldn't have a [{}] extension",
                        url, invalidUrls);
                return false;
            }
        }
        return true;
    }

}
