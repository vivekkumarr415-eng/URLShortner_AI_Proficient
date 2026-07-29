package com.example.urlshortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

public record ShortUrlCreateRequest(
        @NotBlank @Size(max = 2048) @URL(protocol = "https", message = "originalUrl must be a valid HTTPS URL") String originalUrl,
        @Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$", message = "customAlias must contain 3 to 32 URL-safe characters") String customAlias,
        @Future(message = "expiryDate must be in the future") Instant expiryDate
) {
}
