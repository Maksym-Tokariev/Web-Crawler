package com.webcrawler.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HtmlParser {

    /*
     * Parse HTML content and return Document object.
     */
    public Document parse(String htmlContent, String url) {
        log.info("Parsing fo URL: {}", htmlContent);
        return Jsoup.parse(htmlContent, url);
    }
}
