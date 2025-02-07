package com.webcrawler.service.extractor;

import com.webcrawler.service.queue.UrlQueueService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* Extract links from a page and adds them to a queue.
*/

@Slf4j
@Service
@Data
public class UrlExtractor {

    private final UrlQueueService urlQueueService;

    public void extract(Document document) {
        log.info("Extracting urls from document");

        try {
            Elements links = document.select("a[href]");

            List<String> extractedUrls = links.stream().map(link ->
                            link.attr("abs:href"))
                    .filter(this::isValidUrl)
                    .toList();

            extractedUrls.stream().iterator().forEachRemaining(System.out::println);

            urlQueueService.processQueue(extractedUrls);

            log.debug("Extracted url: [{}]", extractedUrls.size());
        } catch (Exception e) {
            log.error("Failed to extract urls: {}", e.getMessage(), e);
        }

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
