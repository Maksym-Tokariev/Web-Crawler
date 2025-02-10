package com.webcrawler.service.extractor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Data
public class KeywordExtractor {

    private static final Set<String> RU_STOP_WORDS = Set.of(
            "и", "в", "во", "не", "что", "он", "на", "я", "с", "со", "как",
            "а", "то", "все", "она", "так", "его", "но", "да", "ты", "к",
            "у", "же", "вы", "за", "бы", "по", "только", "ее", "мне", "было",
            "вот", "от", "меня", "еще", "нет", "о", "из", "ему", "теперь",
            "когда", "даже", "ну", "вдруг", "ли", "если", "уже", "или", "ни",
            "быть", "был", "него", "до", "вас", "нибудь", "опять", "уж", "вам",
            "ведь", "там", "потом", "себя", "ничего", "ей", "может", "они",
            "тут", "где", "есть", "надо", "ней", "для", "мы", "тебя", "их",
            "чем", "была", "сам", "чтоб", "без", "будто", "чего", "раз", "тоже",
            "себе", "под", "будет", "ж", "тогда", "кто", "этот", "того", "потому",
            "этого", "какой", "совсем", "ним", "здесь", "эту", "между"
    );

    public Set<String> extractKeywords(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptySet();
        }

        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("[^\\p{L}\\p{Nd}]+");

        return Arrays.stream(words)
                .filter(word -> word.length() > 2)
                .filter(word -> !RU_STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }
}
