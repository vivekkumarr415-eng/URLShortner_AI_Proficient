package com.example.urlshortener.dto;

import java.time.Instant;

public record ClickAnalyticsResponse(
        Long id,
        String shortCode,
        Instant clickedAt,
        String ipAddress,
        String browser,
        String device,
        String operatingSystem,
        String referrer
) {
}
