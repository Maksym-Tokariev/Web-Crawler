package com.webcrawler.service.loader;

import com.webcrawler.exceptions.ClientErrorException;
import com.webcrawler.exceptions.RedirectException;
import com.webcrawler.exceptions.ServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;

/**
   Responsible for loading websites by URL.
   Processes Http-requests and places page content into class PageContent.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class PageLoader {

    @Value("${loader.max.redirects}")
    private int MAX_REDIRECTS;
    private final WebClient webClient;
    private final RobotsTxtHandler robotsHandler;


    public Mono<PageContent> loadPageWithDelay(String url, long delayMillis) {
        return robotsHandler.isAllowed(url)
                .flatMap(isAllowed -> {
                    if (isAllowed) {
                        return Mono.delay(Duration.ofMillis(delayMillis))
                                .then(loadPage(url, 0));
                    } else {
                        log.error("Access to url: {} denied based on the rules of the robot.txt file", url);
                        return Mono.empty();
                    }
                })
                .onErrorResume(e -> {
                    log.warn("Error checking robots.txt for url: {}. Proceeding with crawling.", url, e);
                    return Mono.delay(Duration.ofMillis(delayMillis))
                            .then(loadPage(url, 0));
                });
    }

    public Mono<PageContent> loadPage(String url, int redirectCount) {
        if (redirectCount > MAX_REDIRECTS) {
            return Mono.error(new RedirectException("Too many redirects", null));
        }

        log.info("Loading page {} ", url);

        return webClient.get()
                .uri(url)
                .exchangeToMono(clientResponse -> {
                    HttpStatusCode statusCode = clientResponse.statusCode();

                    log.info("Status code for {} is {}, processing status", url, statusCode);
                    if (statusCode.is3xxRedirection()) {
                        String location = clientResponse.headers().asHttpHeaders().getFirst(HttpHeaders.LOCATION);

                        if (location != null) {
                            log.debug("Redirect to: {}", location);
                            return loadPage(location, redirectCount + 1);
                        } else {
                            return Mono.error(new RedirectException("Redirect without Location header", null));
                        }

                    } else if (statusCode.is4xxClientError()) {
                        log.warn("Client error: {}, with url: {}", statusCode, url);
                        return Mono.error(new ClientErrorException("Client error: " + statusCode.value()));

                    } else if (statusCode.is5xxServerError()) {
                        log.warn("Server error: {}, with url: {}", statusCode, url);
                        return Mono.error(new ServerErrorException("Server error: " + statusCode.value()));

                    } else {
                        log.info("Successful page loading [{}] with url {}", statusCode, url);
                        return clientResponse.toEntity(String.class)
                                .map(resp -> new PageContent(
                                        url,
                                        resp.getBody(),
                                        resp.getHeaders(),
                                        resp.getStatusCode()
                                ));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error loading page with url: {}, message: {}", url, e.getMessage(), e);
                    return Mono.empty();
                });
    }
}
