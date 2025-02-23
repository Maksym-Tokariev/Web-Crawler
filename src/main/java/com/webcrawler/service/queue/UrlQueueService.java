package com.webcrawler.service.queue;

import com.webcrawler.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * The queue to store a links.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlQueueService {

    public final Sender sender;
    public final Receiver receiver;

    /**
     * Extract URLs from the queue
     */
    public Flux<String> consumeUrls() {
        log.info("Starting to consume URLs from queue: {}", RabbitConfig.QUEUE_NAME);
        return receiver.consumeAutoAck(RabbitConfig.QUEUE_NAME)
                .doOnNext(i -> log.info("Consumed URLs from queue: {}", RabbitConfig.QUEUE_NAME))
                .onBackpressureBuffer(1000)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
                .map(delivery -> new String(delivery.getBody()))
                .onErrorResume(e -> {
                    log.error("RabbitMQ error in consumer: {}", e.getMessage(), e);
                    return Flux.empty();
                })
                .doOnCancel(receiver::close);
    }

    /**
     * Adds URL to the queue.
     */
    public Mono<Void> addUrl(String url) {
        return sender.send(
                Mono.just(new OutboundMessage(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, url.getBytes()))
        ).then();
    }

    /**
     * Adds list of URL's to the queue.
     */
    public Mono<Void> addUrls(List<String> urls) {
        return Flux.fromIterable(urls)
                .flatMap(this::addUrl)
                .then();
    }
}
