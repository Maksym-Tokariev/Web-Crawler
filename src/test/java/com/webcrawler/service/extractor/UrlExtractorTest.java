package com.webcrawler.service.extractor;

import com.webcrawler.model.LinkInfo;
import com.webcrawler.service.DatabaseService;
import com.webcrawler.service.queue.UrlQueueService;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;


public class UrlExtractorTest {

    @Mock
    private UrlQueueService urlQueueService;

    @Mock
    private Deduplicator deduplicator;

    @Mock
    private KeywordExtractor keywordExtractor;

    @Mock
    private DatabaseService databaseService;

    @InjectMocks
    private UrlExtractor urlExtractor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessUrls() {
        List<LinkInfo> urls = List.of(
                new LinkInfo("http://example1.com", Set.of("example")),
                new LinkInfo("http://example2.com", Set.of("example"))
        );

        when(deduplicator.hasUrl(anyString())).thenReturn(Mono.just(true));
        when(databaseService.saveLinkInfo(any(LinkInfo.class))).thenReturn(
                Mono.just(new LinkInfo("http://example1.com", Set.of("example")))
        );
        when(databaseService.saveLinkInfo(any(LinkInfo.class))).thenReturn(
                Mono.just(new LinkInfo("http://example2.com", Set.of("example")))
        );

        StepVerifier.create(urlExtractor.processUrls(urls))
                .expectNextCount(2)
                .verifyComplete();

        verify(deduplicator, times(2)).hasUrl(anyString());
        verify(urlQueueService).addUrls(List.of("http://example1.com", "http://example2.com"));
        verify(databaseService, times(2)).saveLinkInfo(any(LinkInfo.class));
    }

    @Test
    void testProcessUrlsWithDuplicateUrls() {
        List<LinkInfo> extractedUrls = List.of(
                new LinkInfo("http://example1.com", Set.of("example")),
                new LinkInfo("http://example2.com", Set.of("example"))
        );

        when(deduplicator.hasUrl("http://example1.com")).thenReturn(Mono.just(false));
        when(deduplicator.hasUrl("http://example2.com")).thenReturn(Mono.just(true));
        when(databaseService.saveLinkInfo(any(LinkInfo.class))).thenReturn(Mono.just(new LinkInfo("http://example2.com", Set.of("example"))));

        StepVerifier.create(urlExtractor.processUrls(extractedUrls))
                .expectNextMatches(linkInfo ->
                        linkInfo.getUrl().equals("http://example2.com")
                )
                .verifyComplete();

        verify(deduplicator, times(2)).hasUrl(anyString());
        verify(urlQueueService).addUrls(List.of("http://example2.com"));
        verify(databaseService).saveLinkInfo(any(LinkInfo.class));
    }

    @Test
    void testExtractLinkInfo() {
        Element link = new Element(Tag.valueOf("a"), "")
                .attr("abs:href", "http://example.com")
                .attr("title", "Example Title")
                .attr("alt", "Example Alt Text")
                .text("Example Anchor Text");

        Set<String> keywords = Set.of("example", "title", "alt", "text");
        when(keywordExtractor.extractKeywords(anyString())).thenReturn(keywords);

        Optional<LinkInfo> linkInfo = urlExtractor.extractLinkInfo(link);

        Assertions.assertTrue(linkInfo.isPresent());
        Assertions.assertEquals("http://example.com", linkInfo.get().getUrl());
        Assertions.assertEquals(keywords, linkInfo.get().getKeywords());

        verify(keywordExtractor, times(1)).extractKeywords(anyString());
    }

    @Test
    void testExtractLinkInfoInvalidUrl() {
        Element link = new Element(Tag.valueOf("a"), "")
                .attr("abs:href", "ftp://example.com");

        Optional<LinkInfo> linkInfo = urlExtractor.extractLinkInfo(link);

        Assertions.assertFalse(linkInfo.isPresent());

        verify(keywordExtractor, times(0)).extractKeywords(anyString());
    }

    @Test
    void testExtractLinkInfoEmptyKeywords() {
        Element link = new Element(Tag.valueOf("a"), "")
                .attr("abs:href", "http://example.com")
                .attr("title", "Example Title")
                .attr("alt", "Example Alt Text")
                .text("Example Anchor Text");

        when(keywordExtractor.extractKeywords(anyString())).thenReturn(Set.of());

        Optional<LinkInfo> linkInfo = urlExtractor.extractLinkInfo(link);

        Assertions.assertFalse(linkInfo.isPresent());

        verify(keywordExtractor, times(1)).extractKeywords(anyString());
    }

}
