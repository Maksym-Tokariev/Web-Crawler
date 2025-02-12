package com.webcrawler.repository;

import com.webcrawler.model.LinkInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface LinkInfoRepo extends ReactiveMongoRepository<LinkInfo, String> {
    Mono<LinkInfo> findByUrl(String url);
    Mono<LinkInfo> findByKeywordsContaining(String keywords);
}
