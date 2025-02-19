package com.webcrawler.service.loader;

import lombok.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageContent {

    private String url;
    private String htmlContent;
    private HttpHeaders headers;
    private HttpStatusCode statusCode;
}
