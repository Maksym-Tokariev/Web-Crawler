package com.webcrawler.service;

import com.webcrawler.model.LinkInfo;
import com.webcrawler.repository.LinkInfoRepo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * DB service related to the LinkInfo objects.
 * Handles saving a single abject or list of objects to the DB.
 */

@Slf4j
@Data
@Service
public class DatabaseService {

    private final LinkInfoRepo linkInfoRepo;

    /**
     * Save one object to the DB.
     */
    public Mono<LinkInfo> saveLinkInfo(LinkInfo linkInfo) {
        return linkInfoRepo.save(linkInfo)
                .filter(Objects::nonNull)
                .doOnSuccess(savedLinkInfo -> log.trace("LinkInfo for: {} saved", savedLinkInfo.getUrl()))
                .doOnError(e -> log.error("Error in saving link info: {}, message: {}",
                        linkInfo.getUrl(), e.getMessage(), e)
                );
    }

    /**
     * Save list of objects to the DB.
     */
    public Mono<Void> saveAllLinkInfo(List<LinkInfo> linkInfos) {
        return Flux.fromIterable(linkInfos)
                .flatMap(this::saveLinkInfo)
                .then();
    }
}
