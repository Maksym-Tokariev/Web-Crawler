package com.webcrawler.service.extractor;

import com.webcrawler.utils.StopWordLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class KeywordExtractorTest {

    @Mock
    private StopWordLoader stopWordLoader;

    @Mock
    private Lemmatizer lemmatizer;

    @InjectMocks
    KeywordExtractor keywordExtractor;

    @Value("${file.path.stop.words.en}")
    private String STOP_WORDS_EN;

    @Value("${file.path.stop.words.ru}")
    private String STOP_WORDS_RU;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        keywordExtractor = new KeywordExtractor(stopWordLoader, lemmatizer);
    }

    @Test
    void extractKeywords_shouldFilterStopWordsAndLemmatize() {
        when(stopWordLoader.loadStopWord("en_path")).thenReturn(Set.of("and", "the"));
        when(stopWordLoader.loadStopWord("ru_path")).thenReturn(Set.of("и", "в"));

        when(lemmatizer.lemmatize("hello")).thenReturn(Mono.just("hello"));
        when(lemmatizer.lemmatize("мир")).thenReturn(Mono.just("мир"));
        when(lemmatizer.lemmatize("running")).thenReturn(Mono.just("run"));

        String text = "AND the и В hello мир running";

        Mono<Set<String>> result = keywordExtractor.extractKeywords(text);

        StepVerifier.create(result)
                .assertNext(keywords -> {
                    assertThat(keywords).containsExactlyInAnyOrder("hello", "мир", "run");
                })
                .verifyComplete();
    }

    @Test
    void extractKeywords_shouldFilterShortWords() {
        when(stopWordLoader.loadStopWord(anyString())).thenReturn(Set.of());
        when(lemmatizer.lemmatize(anyString())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        String text = "a an it я he us";
        Mono<Set<String>> result = keywordExtractor.extractKeywords(text);

        StepVerifier.create(result)
                .assertNext(keywords -> assertThat(keywords).isEmpty())
                .verifyComplete();
    }

    @Test
    void extractKeywords_shouldSplitWordsCorrectly() {

        when(stopWordLoader.loadStopWord(anyString())).thenReturn(Set.of());
        when(lemmatizer.lemmatize(anyString())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        String text = "word1,word2-word3%word4";
        Mono<Set<String>> result = keywordExtractor.extractKeywords(text);

        StepVerifier.create(result)
                .assertNext(keywords -> {
                    assertThat(keywords).containsExactlyInAnyOrder("word1", "word2", "word3", "word4");
                })
                .verifyComplete();
    }

    @Test
    void extractKeywords_shouldHandleLemmatizationToEmpty() {
        when(stopWordLoader.loadStopWord(anyString())).thenReturn(Set.of());
        when(lemmatizer.lemmatize("ghost")).thenReturn(Mono.empty());
        when(lemmatizer.lemmatize("word")).thenReturn(Mono.just("word"));

        String text = "ghost word";
        Mono<Set<String>> result = keywordExtractor.extractKeywords(text);

        StepVerifier.create(result)
                .assertNext(keywords -> assertThat(keywords).containsExactly("word"))
                .verifyComplete();
    }

    @Test
    void extractKeywords_shouldNotIncludeLemmatizedStopWords() {
        when(stopWordLoader.loadStopWord("en_path")).thenReturn(Set.of("walk"));
        when(stopWordLoader.loadStopWord("ru_path")).thenReturn(Set.of());
        when(lemmatizer.lemmatize("running")).thenReturn(Mono.just("run"));

        String text = "running";
        Mono<Set<String>> result = keywordExtractor.extractKeywords(text);

        StepVerifier.create(result)
                .assertNext(keywords -> assertThat(keywords).containsExactly("run"))
                .verifyComplete();
    }
}
