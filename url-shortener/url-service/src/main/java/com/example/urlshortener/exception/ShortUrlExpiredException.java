package com.example.urlshortener.exception;

public class ShortUrlExpiredException extends RuntimeException {

    public ShortUrlExpiredException(String identifier) {
        super("Short URL has expired: " + identifier);
    }
}
