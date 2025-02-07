package com.webcrawler.service.parser;

import com.webcrawler.service.Deduplicator;
import com.webcrawler.service.extractor.UrlExtractor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Data
public class HtmlParser {

    private final UrlExtractor urlExtractor;
    private final Deduplicator deduplicator;

    /**
     * Parse HTML content and return Document object.
     */
    public void parse(String htmlContent, String url) {
        log.info("Parsing for URL: {}", url);
        Document document = Jsoup.parse(htmlContent, url);

        deduplicator.addUrl(url);
        urlExtractor.extract(document);

    }
}
