package com.webcrawler.service.extractor;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LemmatizerTest {

    @Mock
    private StanfordCoreNLP stanfordCoreNLP;

    @InjectMocks
    private Lemmatizer lemmatizer;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Properties prop = new Properties();
        prop.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        when(stanfordCoreNLP.getProperties()).thenReturn(prop);
        lemmatizer.init();
    }

    @Test
    void testLemmatizeWord() {
        String[] words = {
                "running", "cats", "beautiful",
                "driven", "doing", "mice",
                "faster", "studied", "walking"
        };
        String[] lemmas = {
                "run", "cat", "beautiful",
                "drive", "do", "mouse",
                "fast", "study", "walk"
        };

        for (int i = 0; i < words.length; i++) {
            Annotation document = new Annotation(words[i]);
            CoreLabel token = new CoreLabel();
            token.setWord(words[i]);
            token.setLemma(lemmas[i]);

            document.set(CoreAnnotations.TokensAnnotation.class, Collections.singletonList(token));

            when(stanfordCoreNLP.process(any(String.class))).thenAnswer(invocation -> {
                Annotation doc = invocation.getArgument(0);
                doc.set(CoreAnnotations.TokensAnnotation.class, Collections.singletonList(token));
                return null;
            });

            String res = lemmatizer.lemmatizeWord(words[i]);

            assertEquals(lemmas[i], res);
        }
    }

    @Test
    void testLemmatizeWordWithNull() {
        String result = lemmatizer.lemmatizeWord(null);
        assertNull(result);
    }

    @Test
    void testLemmatizeWordWithEmptyString() {
        String result = lemmatizer.lemmatizeWord("");
        assertNull(result);
    }

}
