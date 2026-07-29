package com.example.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(
        name = "click_analytics",
        indexes = {
                @Index(name = "idx_click_analytics_short_code_clicked_at", columnList = "short_code, clicked_at"),
                @Index(name = "idx_click_analytics_clicked_at", columnList = "clicked_at")
        }
)
public class ClickAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$", message = "shortCode must contain 3 to 32 URL-safe characters")
    @Column(name = "short_code", nullable = false, length = 32)
    private String shortCode;

    @NotNull
    @Column(name = "clicked_at", nullable = false, updatable = false)
    private Instant clickedAt;

    @NotBlank
    @Pattern(regexp = "^[0-9a-fA-F:.]+$", message = "ipAddress must be an IPv4 or IPv6 address")
    @Size(max = 45)
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String browser;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String device;

    @NotBlank
    @Size(max = 255)
    @Column(name = "operating_system", nullable = false, length = 255)
    private String operatingSystem;

    @Size(max = 2048)
    @Column(length = 2048)
    private String referrer;

    protected ClickAnalytics() {
    }

    public ClickAnalytics(String shortCode, Instant clickedAt, String ipAddress, String browser, String device,
                          String operatingSystem, String referrer) {
        this.shortCode = shortCode;
        this.clickedAt = clickedAt;
        this.ipAddress = ipAddress;
        this.browser = browser;
        this.device = device;
        this.operatingSystem = operatingSystem;
        this.referrer = referrer;
    }

    @PrePersist
    void initializeClickedAt() {
        if (clickedAt == null) {
            clickedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Instant getClickedAt() {
        return clickedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getBrowser() {
        return browser;
    }

    public String getDevice() {
        return device;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getReferrer() {
        return referrer;
    }
}
