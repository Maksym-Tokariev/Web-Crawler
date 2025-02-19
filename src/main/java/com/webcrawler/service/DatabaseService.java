package com.webcrawler.service;

import com.webcrawler.model.LinkInfo;
import com.webcrawler.repository.LinkInfoRepo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Data
@Service
public class DatabaseService {

    private final LinkInfoRepo linkInfoRepo;

    public Mono<LinkInfo> saveLinkInfo(LinkInfo linkInfo) {
        return linkInfoRepo.save(linkInfo)
                .filter(Objects::nonNull)
                .doOnSuccess(savedLinkInfo -> log.info("LinkInfo for: {} saved", savedLinkInfo.getUrl()))
                .doOnError(e -> log.error("Error in saving link info: {}, message: {}",
                        linkInfo.getUrl(), e.getMessage(), e)
                );
    }
}
