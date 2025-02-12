package com.webcrawler.service.extractor;

import com.webcrawler.utils.StopWordLoader;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class removes stop words from the text and performs word stemming.
 */

@Slf4j
@Service
@Data
public class KeywordExtractor {

    private final StopWordLoader stopWordLoader;
    @Value("${file.path.stop.words.en}")
    private String STOP_WORDS_EN;

    @Value("${file.path.stop.words.ru}")
    private String STOP_WORDS_RU;

    public Set<String> extractKeywords(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptySet();
        }

        log.info("Extracting keywords from text {}", text);

        Set<String> stopEn = stopWordLoader.loadStopWord(STOP_WORDS_EN);
        Set<String> stopRu = stopWordLoader.loadStopWord(STOP_WORDS_RU);

        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("[^\\p{L}\\p{Nd}]+");

        return Arrays.stream(words)
                .filter(word -> word.length() > 2)
                .filter(word -> !stopEn.contains(word))
                .filter(word -> !stopRu.contains(word))
                .map(String::toLowerCase)
                .map(this::stemWord)
                .collect(Collectors.toSet());
    }

    private String stemWord(String word) {
        try {
            log.debug("Stem word: {}", word);
            Analyzer analyzer = new StandardAnalyzer();
            TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(word));
            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            String res = null;
            if (tokenStream.incrementToken()) {
                res = termAttribute.toString();
            }
            tokenStream.end();
            tokenStream.close();
            analyzer.close();

            log.trace("Stemmed word: {}", res);
            return res;
        } catch (IOException e) {
            log.error("Failed to stem word: {}", word, e);
            return null;
        }
    }
}
