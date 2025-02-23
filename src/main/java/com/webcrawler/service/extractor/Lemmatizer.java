package com.webcrawler.service.extractor;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class Lemmatizer {

    private StanfordCoreNLP stanfordCoreNLP;

    @PostConstruct
    public void init() {
        Properties prop = new Properties();
        prop.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        stanfordCoreNLP = new StanfordCoreNLP(prop);
    }

    public Mono<String> lemmatize(String word) {
        return Mono.fromCallable(() -> lemmatizeWord(word))
                .filter(lemma -> lemma != null && !lemma.isEmpty())
                .subscribeOn(Schedulers.boundedElastic());
    }

    public String lemmatizeWord(String word) {
        log.trace("Lemmatize word [{}]", word);

        if (word == null || word.isEmpty()) {
            return null;
        }

        Annotation document = new Annotation(word);
        stanfordCoreNLP.annotate(document);

        return document.get(CoreAnnotations.TokensAnnotation.class).stream()
                .findFirst()
                .map(token -> token.get(CoreAnnotations.LemmaAnnotation.class))
                .filter(lemma -> !"O".equals(lemma))
                .orElse(null);
    }
}
