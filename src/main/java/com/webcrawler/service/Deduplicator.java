package com.webcrawler.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;


/**
 * A class to store url's that have already been visited
 */

@Slf4j
@Service
public class Deduplicator {

    private final ConcurrentHashMap<String, Boolean> urls = new ConcurrentHashMap<>();

    public void addUrl(String url) {
        urls.put(url, true);
        log.debug("Added to deduplicator URL: {}", url);
    }

    public boolean isNewUrl(String url) {
        return urls.putIfAbsent(url, Boolean.TRUE) == null;
    }
}
