package com.webcrawler.config;


import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/*
* Configure the WebClient.
*/

@Configuration
public class LoaderConfig {

    @Value("${loader.connect.timeout}")
    private int TIMEOUT;

    @Value("${loader.response.timeout}")
    private int RESPONSE_TIMEOUT;

    @Value("${loader.read.timeout}")
    private int READE_TIMEOUT;

    @Value("${loader.write.timeout}")
    private int WRITE_TIMEOUT;

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(READE_TIMEOUT))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("User-Agent", "MyWebCrawler/1.0")
                .build();
    }
}
