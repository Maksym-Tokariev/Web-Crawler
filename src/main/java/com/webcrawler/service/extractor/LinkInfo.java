package com.webcrawler.service.extractor;

import java.util.Set;

public class LinkInfo {
    private final String url;
    private Set<String> keywords;

    public LinkInfo(String url, Set<String> keywords) {
        this.url = url;
        this.keywords = keywords;
    }

    public String getUrl() {
        return url;
    }
    public Set<String> getKeywords() {
        return keywords;
    }
}
