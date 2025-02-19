package com.webcrawler.service;


import com.webcrawler.model.LinkInfo;
import com.webcrawler.repository.LinkInfoRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.mockito.Mockito.*;

public class DatabaseServiceTest {

    @Mock
    private LinkInfoRepo linkInfoRepoMock;

    @InjectMocks
    private DatabaseService databaseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveLinkInfoSuccess() {
        LinkInfo linkInfo = new LinkInfo("http://example.com", Set.of("keywords1", "keywords2"));

        when(linkInfoRepoMock.save(any(LinkInfo.class))).thenReturn(Mono.just(linkInfo));

        Mono<LinkInfo> result = databaseService.saveLinkInfo(linkInfo);

        StepVerifier.create(result)
                .expectNext(linkInfo)
                .verifyComplete();

        verify(linkInfoRepoMock, times(1)).save(linkInfo);
    }

    @Test
    void testSaveLinkInfoError() {
        LinkInfo linkInfo = new LinkInfo("http://example.com", Set.of("keywords1", "keywords2"));

        when(linkInfoRepoMock.save(any(LinkInfo.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<LinkInfo> result = databaseService.saveLinkInfo(linkInfo);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Database error"))
                .verify();

        verify(linkInfoRepoMock, times(1)).save(linkInfo);
    }

    @Test
    void testSaveLinkInfoNullValue() {

        when(linkInfoRepoMock.save(any(LinkInfo.class))).thenReturn(
                Mono.error(new NullPointerException("URL must not be null"))
        );

        LinkInfo linkInfo = new LinkInfo("http://example.com", null);

        Mono<LinkInfo> result = databaseService.saveLinkInfo(linkInfo);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof NullPointerException  &&
                        e.getMessage().equals("URL must not be null"))
                .verify();

        LinkInfo linkInfo2 = new LinkInfo(null, Set.of("keywords1", "keywords2"));

        Mono<LinkInfo> result2 = databaseService.saveLinkInfo(linkInfo2);

        StepVerifier.create(result2)
                .expectErrorMatches(e -> e instanceof NullPointerException  &&
                        e.getMessage().equals("URL must not be null"))
                .verify();
    }

    @Test
    void testSaveLinkInfoEmptyValue() {
        LinkInfo linkInfo = new LinkInfo("", Set.of());

        when(linkInfoRepoMock.save(any(LinkInfo.class))).thenReturn(
                Mono.error(new IllegalArgumentException("URL must not be empty"))
        );

        Mono<LinkInfo> result = databaseService.saveLinkInfo(linkInfo);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("URL must not be empty"))
                .verify();
    }
}
