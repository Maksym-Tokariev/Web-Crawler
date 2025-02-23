package com.webcrawler.service.extractor;

import com.webcrawler.model.LinkInfo;
import com.webcrawler.service.DatabaseService;
import com.webcrawler.service.queue.UrlQueueService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UrlExtractorTest {

    @Mock
    private Element element;

    @Mock
    private Document document;

    @Mock
    private Elements elements;

    @Mock
    private UrlQueueService urlQueueService;

    @Mock
    private Deduplicator deduplicator;

    @Mock
    private KeywordExtractor keywordExtractor;

    @Mock
    private DatabaseService databaseService;

    @InjectMocks
    private UrlExtractor extractor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExtract() {
        Element element1 = new Element("a");
        element1.attr("abs:href", "https://example.com");
        element1.text("Example Link 1");

        Element element2 = new Element("a");
        element2.attr("abs:href", "https://example2.com");
        element2.text("Example Link 2");

        when(document.title()).thenReturn("Title");
        when(elements.iterator()).thenReturn(List.of(element1, element2).iterator());
        when(document.select("a[href]")).thenReturn(elements);

        when(keywordExtractor.extractKeywords(any(String.class)))
                .thenReturn(Mono.just(Set.of("example")));

        when(deduplicator.isNotProcessed("https://example.com")).thenReturn(Mono.just(true));
        when(deduplicator.isNotProcessed("https://example2.com")).thenReturn(Mono.just(false));

        when(databaseService.saveAllLinkInfo(any(List.class))).thenReturn(Mono.empty());
        when(urlQueueService.addUrls(any(List.class))).thenReturn(Mono.empty());

        Mono<List<LinkInfo>> result = extractor.extract(document);

        result.subscribe(actualLinkInfos -> {
            assertEquals(1, actualLinkInfos.size());
            assertEquals("https://example.com", actualLinkInfos.get(0).getUrl());
        });

        verify(document, times(1)).select("a[href]");
        verify(keywordExtractor, times(2)).extractKeywords(any(String.class));
        verify(deduplicator, times(1)).isNotProcessed("https://example.com");
        verify(deduplicator, times(1)).isNotProcessed("https://example2.com");
        verify(databaseService, times(1)).saveAllLinkInfo(any(List.class));
        verify(urlQueueService, times(1)).addUrls(any(List.class));
    }

    @Test
    void testProcessUrl() {
        List<LinkInfo> linkInfos = new ArrayList<>();
        linkInfos.add(new LinkInfo("https://example.com", Set.of("example")));
        linkInfos.add(new LinkInfo("https://example2.com", Set.of("example2")));

        when(deduplicator.isNotProcessed(anyString())).thenReturn(Mono.just(true));
        when(databaseService.saveAllLinkInfo(any(List.class))).thenReturn(Mono.empty());
        when(urlQueueService.addUrls(any(List.class))).thenReturn(Mono.empty());

        Mono<List<LinkInfo>> res = extractor.processUrls(linkInfos);

        res.subscribe(linkInfos1 -> {
            assertEquals(1, linkInfos1.size());
            assertEquals("https://example.com", linkInfos1.get(0).getUrl());
        });

        verify(deduplicator, times(1)).isNotProcessed("https://example.com");
        verify(deduplicator, times(1)).isNotProcessed("https://example2.com");
        verify(databaseService, times(1)).saveAllLinkInfo(any(List.class));
        verify(urlQueueService, times(1)).addUrls(any(List.class));
    }

    @Test
    void testGetLinkTextEmptyTitleAndAlt() {
        when(element.attr("abs:href")).thenReturn("https://example.com");
        when(element.text()).thenReturn("Example Link");
        when(element.attr("title")).thenReturn("");
        when(element.attr("alt")).thenReturn("");

        String expectedLinkText = "Example Link";
        String actualLinkText = extractor.getLinkText(element);

        assertEquals(expectedLinkText, actualLinkText);
    }

    @Test
    void testGetLinkTextEmptyAlt() {
        when(element.attr("abs:href")).thenReturn("https://example.com");
        when(element.text()).thenReturn("Example Link");
        when(element.attr("title")).thenReturn("Title");
        when(element.attr("alt")).thenReturn("");

        String expectedLinkText = "Example Link Title";
        String actualLinkText = extractor.getLinkText(element);

        assertEquals(expectedLinkText, actualLinkText);
    }

    @Test
    void testGetLinkTextEmptyTitle() {
        when(element.attr("abs:href")).thenReturn("https://example.com");
        when(element.text()).thenReturn("Example Link");
        when(element.attr("title")).thenReturn("");
        when(element.attr("alt")).thenReturn("Alt");

        String expectedLinkText = "Example Link Alt";
        String actualLinkText = extractor.getLinkText(element);

        assertEquals(expectedLinkText, actualLinkText);
    }

    @Test
    void testGetLinkTextEmptyText() {
        when(element.attr("abs:href")).thenReturn("https://example.com");
        when(element.text()).thenReturn("");
        when(element.attr("title")).thenReturn("Title");
        when(element.attr("alt")).thenReturn("Alt");

        String expectedLinkText = "Title Alt";
        String actualLinkText = extractor.getLinkText(element);

        assertEquals(expectedLinkText, actualLinkText);
    }

}
