package com.webcrawler.exceptions;

import lombok.Getter;

@Getter
public class RedirectException extends RuntimeException {
    private String location;

    public RedirectException(String message, String location) {
        super(message);
        this.location = location;
    }

}
