package com.example.urlshortener.dto;

import java.time.Instant;

public record ShortUrlResponse(
        Long id,
        String originalUrl,
        String shortCode,
        String customAlias,
        Instant createdAt,
        Instant expiryDate,
        boolean active,
        long clickCount
) {
}
