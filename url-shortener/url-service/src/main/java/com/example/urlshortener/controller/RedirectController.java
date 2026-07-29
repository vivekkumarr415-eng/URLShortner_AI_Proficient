package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import com.example.urlshortener.exception.ApiError;
import com.example.urlshortener.service.AnalyticsEventPublisher;
import com.example.urlshortener.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Instant;

@RestController
@Tag(name = "Redirects", description = "Public short URL redirects")
@Validated
public class RedirectController {

    private final ShortUrlService shortUrlService;
    private final AnalyticsEventPublisher analyticsEventPublisher;

    public RedirectController(ShortUrlService shortUrlService, AnalyticsEventPublisher analyticsEventPublisher) {
        this.shortUrlService = shortUrlService;
        this.analyticsEventPublisher = analyticsEventPublisher;
    }

    @GetMapping("/r/{shortCode}")
    @Operation(summary = "Redirect to the original URL")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirected to original URL"),
            @ApiResponse(responseCode = "404", description = "Short URL not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "410", description = "Short URL is inactive or expired", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> redirect(@PathVariable @Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$") String shortCode,
                                         HttpServletRequest request) {
        String destination = shortUrlService.resolveDestination(shortCode);
        analyticsEventPublisher.record(new ClickAnalyticsCreateRequest(shortCode, Instant.now(), request.getRemoteAddr(),
                browser(request.getHeader(HttpHeaders.USER_AGENT)), device(request.getHeader(HttpHeaders.USER_AGENT)),
                operatingSystem(request.getHeader(HttpHeaders.USER_AGENT)), request.getHeader(HttpHeaders.REFERER)));
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create(destination).toASCIIString())
                .build();
    }

    private String browser(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Edg/")) return "Edge";
        if (userAgent.contains("Chrome/")) return "Chrome";
        if (userAgent.contains("Firefox/")) return "Firefox";
        if (userAgent.contains("Safari/")) return "Safari";
        return "Other";
    }

    private String device(String userAgent) {
        if (userAgent == null) return "Unknown";
        String normalized = userAgent.toLowerCase();
        if (normalized.contains("tablet") || normalized.contains("ipad")) return "Tablet";
        if (normalized.contains("mobile") || normalized.contains("android")) return "Mobile";
        return "Desktop";
    }

    private String operatingSystem(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        if (userAgent.contains("Mac OS X")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        return "Other";
    }
}
