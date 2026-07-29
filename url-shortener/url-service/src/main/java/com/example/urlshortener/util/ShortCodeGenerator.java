package com.example.urlshortener.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int CODE_LENGTH = 8;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        char[] value = new char[CODE_LENGTH];
        for (int index = 0; index < CODE_LENGTH; index++) {
            value[index] = ALPHABET[secureRandom.nextInt(ALPHABET.length)];
        }
        return new String(value);
    }
}
