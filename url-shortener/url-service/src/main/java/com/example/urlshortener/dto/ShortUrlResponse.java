package com.example.urlshortener.dto;

import java.time.Instant;

public record ShortUrlResponse(
        Long id,
        String originalUrl,
        String shortCode,
        String customAlias,
        String publicCode,
        Instant createdAt,
        Instant expiryDate,
        boolean active,
        long clickCount
) {
}
