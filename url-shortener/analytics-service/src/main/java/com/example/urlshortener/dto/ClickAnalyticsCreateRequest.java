package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ClickAnalyticsCreateRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$", message = "shortCode must contain 3 to 32 URL-safe characters") String shortCode,
        @NotNull Instant clickedAt,
        @NotBlank @Pattern(regexp = "^[0-9a-fA-F:.]+$", message = "ipAddress must be an IPv4 or IPv6 address") @Size(max = 45) String ipAddress,
        @NotBlank @Size(max = 255) String browser,
        @NotBlank @Size(max = 255) String device,
        @NotBlank @Size(max = 255) String operatingSystem,
        @Size(max = 2048) String referrer
) {
}
