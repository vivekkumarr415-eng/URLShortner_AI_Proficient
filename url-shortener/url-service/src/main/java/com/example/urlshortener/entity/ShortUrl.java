package com.example.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

@Entity
@Table(
        name = "short_urls",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_short_urls_short_code", columnNames = "short_code"),
                @UniqueConstraint(name = "uk_short_urls_custom_alias", columnNames = "custom_alias")
        },
        indexes = {
                @Index(name = "idx_short_urls_active_expiry", columnList = "active, expiry_date"),
                @Index(name = "idx_short_urls_created_at", columnList = "created_at")
        }
)
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 2048)
    @URL(protocol = "https", message = "originalUrl must be a valid HTTPS URL")
    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_-]{6,32}$", message = "shortCode must contain 6 to 32 URL-safe characters")
    @Column(name = "short_code", nullable = false, length = 32)
    private String shortCode;

    @Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$", message = "customAlias must contain 3 to 32 URL-safe characters")
    @Column(name = "custom_alias", length = 32)
    private String customAlias;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Future(message = "expiryDate must be in the future")
    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean active;

    @Min(0)
    @Column(name = "click_count", nullable = false)
    private long clickCount;

    protected ShortUrl() {
    }

    public ShortUrl(String originalUrl, String shortCode, String customAlias, Instant expiryDate, boolean active, long clickCount) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.customAlias = customAlias;
        this.expiryDate = expiryDate;
        this.active = active;
        this.clickCount = clickCount;
    }

    @PrePersist
    void initializeCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public long getClickCount() {
        return clickCount;
    }
}
