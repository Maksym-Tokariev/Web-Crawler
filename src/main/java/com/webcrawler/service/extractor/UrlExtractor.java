package com.webcrawler.service.extractor;

import com.webcrawler.service.DatabaseService;
import com.webcrawler.service.queue.UrlQueueService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
        try {
            log.info("Extracting urls from document: {}", document.title());

            Elements links = document.select("a[href]");
            Elements meta = document.select("meta[name=description, meta=keywords]"); //TODO

//            List<String> extractedUrls = links.stream().map(link ->
//                            link.attr("abs:href"))
//                    .filter(this::isValidUrl)
//                    .filter(url -> {
//                        if (!deduplicator.isNewUrl(url)) {
//                            log.info("Skipping the duplicate url: {}", url);
//                            return false;
//                        }
//                        return true;
//                    })
//                    .toList();

            List<LinkInfo> extractedUrls = links.stream()
                    .map(this::extractLinkInfo)
                    .filter(Objects::nonNull)
                    .toList();


            List<String> urls = extractedUrls.stream()
                    .map(LinkInfo::getUrl)
                    .filter(Objects::nonNull)
                    .filter(url -> {
                        if (!deduplicator.isNewUrl(url)) {
                            log.info("Skipping the duplicate url: {}", url);
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            urls.stream().iterator().forEachRemaining(System.out::println);
            urlQueueService.addUrls(urls);

//            extractedUrls.forEach(databaseService::saveLinkInfo);

            log.info("Extracted and saved {} links with keywords", extractedUrls.size());
        } catch (Exception e) {
            log.error("Failed to extract urls: {}", e.getMessage(), e);
        }

    }

    private LinkInfo extractLinkInfo(Element link) {
        String url = link.attr("abs:href");
        if (!isValidUrl(url)) {
            return null;
        }

        String anchorText = link.text().trim();

        String title = link.attr("title").trim();
        String alt = link.attr("alt").trim();

        String text = Stream.of(anchorText, title, alt)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));

        Set<String> keywords = keywordExtractor.extractKeywords(text);

        return new LinkInfo(url, keywords);
    }

    private String stemWord(String word) {
        // TODO
        return "";
    }

    public boolean isValidUrl(String url) {
        if (!url.startsWith("http")) {
            return false;
        }
        String[] invalidUrls = {".jpg", ".png", ".gif", ".pdf", ".zip", ".rar", ".exe" };
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
