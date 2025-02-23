package com.webcrawler.service.extractor;

import com.webcrawler.utils.StopWordLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
   This class removes stop words from the text and performs word lemming.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordExtractor {

    private final StopWordLoader stopWordLoader;

    private final Lemmatizer lemmatizer;

    @Value("${file.path.stop.words.en}")
    private String STOP_WORDS_EN;

    @Value("${file.path.stop.words.ru}")
    private String STOP_WORDS_RU;

    public Mono<Set<String>> extractKeywords(String text) {
        Set<String> stopEn = stopWordLoader.loadStopWord(STOP_WORDS_EN);
        Set<String> stopRu = stopWordLoader.loadStopWord(STOP_WORDS_RU);

        return Flux.fromArray(text.toLowerCase().split("[^\\p{L}\\p{Nd}]+"))
                .filter(word -> word.length() > 2)
                .filter(word -> !stopEn.contains(word))
                .filter(word -> !stopRu.contains(word))
                .flatMap(lemmatizer::lemmatize)
                .collect(Collectors.toSet());
    }
}
