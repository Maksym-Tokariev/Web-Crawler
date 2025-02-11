package com.webcrawler.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@Data
public class StopWordLoader {

    public Set<String> loadStopWord(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            Set<String> stopWords = new HashSet<>();
            String line;
            while ((line = br.readLine()) != null) {
                stopWords.add(line.trim());
            }
            return stopWords;
        } catch (IOException e) {
            log.error("Error while load stop words: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
