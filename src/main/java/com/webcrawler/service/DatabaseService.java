package com.webcrawler.service;

import com.webcrawler.model.LinkInfo;
import com.webcrawler.repository.LinkInfoRepo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Data
@Service
public class DatabaseService {

    private final LinkInfoRepo linkInfoRepo;

    public Mono<LinkInfo> saveLinkInfo(LinkInfo linkInfo) {

        log.info("LinkInfo: {} saved", linkInfo);
        return linkInfoRepo.save(linkInfo)
                .doOnSuccess(savedLinkInfo -> log.info("LinkInfo: {} saved", savedLinkInfo))
                .onErrorResume(e -> {
                    log.error("Error in saving link info: {}, message: {}",
                        linkInfo, e.getMessage(), e);
                    return Mono.empty();
                });
    }
}
