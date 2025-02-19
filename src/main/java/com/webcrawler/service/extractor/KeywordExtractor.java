package com.webcrawler.service.extractor;

import com.webcrawler.utils.StopWordLoader;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
   This class removes stop words from the text and performs word lemming.
 */

@Slf4j
@Service
@Data
public class KeywordExtractor {

    private final StopWordLoader stopWordLoader;

    private StanfordCoreNLP stanfordCoreNLP;

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
                .map(this::lemmatizeWord)
                .filter(Optional::isPresent)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    public Optional<String> lemmatizeWord(String word) {
        log.debug("Lemmatize word {}", word);

        if (word == null || word.isEmpty()) {
            return Optional.empty();
        }

        Properties prop = new Properties();
        prop.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        stanfordCoreNLP = new StanfordCoreNLP(prop);

        Annotation document = new Annotation(word);
        stanfordCoreNLP.annotate(document);

        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
        for (CoreLabel token : tokens) {
            String posTag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

            log.trace("Token: {}, POS Tag: {}, Lemma: {}", token.originalText(), posTag, lemma);
            if (lemma != null && !lemma.equals("O")) {
                return Optional.of(lemma);
            }
        }
        return Optional.empty();
    }
}
