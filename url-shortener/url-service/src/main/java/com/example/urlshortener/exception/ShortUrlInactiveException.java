package com.example.urlshortener.exception;

public class ShortUrlInactiveException extends RuntimeException {

    public ShortUrlInactiveException(String identifier) {
        super("Short URL is inactive: " + identifier);
    }
}
