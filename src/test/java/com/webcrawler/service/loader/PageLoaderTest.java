package com.webcrawler.service.loader;

import com.sun.net.httpserver.HttpServer;
import com.webcrawler.exceptions.ClientErrorException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

public class PageLoaderTest {

    @Test
    public void testLoadPageWithDelay_LocalServer() throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/test", exchange -> {
            String response = "<html><body>Test Page</body></html>";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });
        server.start();

        String url = "http://localhost:8080/test";

        WebClient webClient = WebClient.create();
        RobotsTxtHandler robotsHandler = new RobotsTxtHandler() {
            @Override
            public Mono<Boolean> isAllowed(String url) {
                return Mono.just(true);
            }
        };

        PageLoader pageLoader = new PageLoader(webClient, robotsHandler);

        try {
            PageContent pageContent = pageLoader.loadPageWithDelay(url, 0).block();

            assertNotNull(pageContent);
            assertEquals(HttpStatus.OK.value(), pageContent.getStatusCode().value());
            assertEquals("<html><body>Test Page</body></html>", pageContent.getHtmlContent());

        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testLoadPageWithDelay_Redirection() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/redirect", exchange -> {
            String newLocation = "http://localhost:8080/redirected";
            exchange.getResponseHeaders().add("Location", newLocation);
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.createContext("/redirected", exchange -> {
            String response = "<html><body>Redirected Page</body></html>";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });

        server.start();

        String url = "http://localhost:8080/redirect";

        WebClient webClient = WebClient.create();
        RobotsTxtHandler robotsHandler = new RobotsTxtHandler() {
            @Override
            public Mono<Boolean> isAllowed(String url) {
                return Mono.just(true);
            }
        };

        PageLoader pageLoader = new PageLoader(webClient, robotsHandler);

        try {
            PageContent pageContent = pageLoader.loadPageWithDelay(url, 0).block();

            assertNotNull(pageContent);
            assertEquals("http://localhost:8080/redirected", pageContent.getUrl());
            assertEquals(HttpStatus.OK.value(), pageContent.getStatusCode().value());
            assertEquals("<html><body>Redirected Page</body></html>", pageContent.getHtmlContent());

        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testLoadPageWithDelay_ClientError() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/error", exchange -> {
           String response = "Page not found";
           exchange.sendResponseHeaders(404, response.length());
           exchange.getResponseBody().write(response.getBytes());
           exchange.getResponseBody().close();
        });

        server.start();
        String url = "http://localhost:8080/error";

        WebClient webClient = WebClient.create();
        RobotsTxtHandler robotsHandler = new RobotsTxtHandler() {
            @Override
            public Mono<Boolean> isAllowed(String url) {
                return Mono.just(true);
            }
        };

        PageLoader pageLoader = new PageLoader(webClient, robotsHandler);

        try {
            pageLoader.loadPageWithDelay(url, 0)
                    .doOnError(e -> {
                        assertTrue(e instanceof ClientErrorException);
                        assertEquals("Client error 404 while requesting http://localhost:8080/error: Page not found", e.getMessage());
                    })
                    .block();

        } catch (Exception e) {
          Throwable cause = e.getCause();
          assertTrue(cause instanceof ClientErrorException);
          assertEquals("Client error 404 while requesting http://localhost:8080/error: Page not found", e.getMessage());
        } finally {
            server.stop(0);
        }
    }
}
