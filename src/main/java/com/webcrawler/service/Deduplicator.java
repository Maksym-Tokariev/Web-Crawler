package com.webcrawler.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


/**
 * A class to store url's that have already been visited
 */

@Slf4j
@Service
@Data
public class Deduplicator {

    private final Set<String> urls = new HashSet<>();

    public void addUrl(String url) {
        urls.add(url);
    }

    public boolean wasVisited(String url) {
        log.info("!----List oo duplicates: {}", urls.stream().toList());
        return urls.contains(url);
    }
}
