package com.example.urlshortener.exception;

public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException(String identifier) {
        super("Short URL was not found for identifier: " + identifier);
    }
}
