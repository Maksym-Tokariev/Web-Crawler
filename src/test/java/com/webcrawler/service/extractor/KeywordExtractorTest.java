package com.webcrawler.service.extractor;


import com.webcrawler.utils.StopWordLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

public class KeywordExtractorTest {

    @Mock
    private StopWordLoader stopWordLoader;

    @InjectMocks
    private KeywordExtractor keywordExtractor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        keywordExtractor.setSTOP_WORDS_EN("src/main/resources/stop_words_en.txt");
        keywordExtractor.setSTOP_WORDS_RU("src/main/resources/stop_words_ru.txt");
    }

    @Test
    void testExtractKeyword() {
        String testText = "This is a simple example. Простой пример.";

        Set<String> stopWordsEn = new HashSet<>();
        stopWordsEn.add("this");
        stopWordsEn.add("is");
        stopWordsEn.add("a");

        Set<String> stopWordsRu = new HashSet<>();
        stopWordsRu.add("простой");

        when(stopWordLoader.loadStopWord(keywordExtractor.getSTOP_WORDS_EN())).thenReturn(stopWordsEn);
        when(stopWordLoader.loadStopWord(keywordExtractor.getSTOP_WORDS_RU())).thenReturn(stopWordsRu);

        Set<String> expectedKeywords = new HashSet<>();
        expectedKeywords.add("simple");
        expectedKeywords.add("example");
        expectedKeywords.add("пример");

        Set<String> keywords = keywordExtractor.extractKeywords(testText);

        Assertions.assertNotNull(keywords);
        Assertions.assertEquals(expectedKeywords, keywords);
    }

    @Test
    void testExtractKeywordsEmptyText() {
        Set<String> keywords = keywordExtractor.extractKeywords("");
        Assertions.assertNotNull(keywords);
        Assertions.assertTrue(keywords.isEmpty());
    }

    @Test
    void testExtractKeywordsNullText() {
        Set<String> keywords = keywordExtractor.extractKeywords(null);
        Assertions.assertNotNull(keywords);
        Assertions.assertTrue(keywords.isEmpty());
    }

    @Test
    void testStemWord() {
        String[] enWords = {"running", "cars", "went", "flies", "children", "easily", "stronger"};
        String[] enStems = {"run","car", "go", "fly", "child", "easily", "strong"};

        for (int i = 0; i < enWords.length; i++) {
            Optional<String> enStemWord = keywordExtractor.lemmatizeWord(enWords[i]);

            Assertions.assertTrue(enStemWord.isPresent());
            Assertions.assertEquals(enStems[i], enStemWord.get());
        }
    }

    @Test
    public void testStemWordNonStemmable() {
        String word = "example";
        String expectedStem = "example";

        Optional<String> actualStem = keywordExtractor.lemmatizeWord(word);

        Assertions.assertTrue(actualStem.isPresent());
        Assertions.assertEquals(expectedStem, actualStem.get());
    }

    @Test
    public void testStemWordNull() {
        String word = null;
        Optional<String> actualStem = keywordExtractor.lemmatizeWord(word);

        Assertions.assertTrue(actualStem.isEmpty());
    }
}
