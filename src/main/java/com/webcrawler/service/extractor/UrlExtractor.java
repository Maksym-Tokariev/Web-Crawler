package com.webcrawler.service.extractor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Data
public class UrlExtractor {

    public void extract(Document document) {
        Elements links = document.getElementsByAttribute("href");
        for (Element link : links) {
            String url = link.attr("abs:href");
            System.out.println(url);
            String title = link.text();
        }
    }

}
