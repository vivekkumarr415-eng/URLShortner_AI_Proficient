package com.example.urlshortener.exception;

public class DuplicateAliasException extends RuntimeException {

    public DuplicateAliasException(String alias) {
        super("The custom alias is already in use: " + alias);
    }
}
