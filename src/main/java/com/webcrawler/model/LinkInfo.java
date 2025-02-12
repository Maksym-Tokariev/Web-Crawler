package com.webcrawler.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Getter
@Setter
@Document(collation = "links")
public class LinkInfo {
    private final String url;
    private final Set<String> keywords;

    public LinkInfo(String url, Set<String> keywords) {
        this.url = url;
        this.keywords = keywords;
    }
}
