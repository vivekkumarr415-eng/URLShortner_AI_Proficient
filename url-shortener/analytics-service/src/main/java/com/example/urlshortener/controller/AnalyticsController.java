package com.example.urlshortener.controller;

import com.example.urlshortener.dto.AnalyticsSummaryResponse;
import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import com.example.urlshortener.dto.ClickAnalyticsResponse;
import com.example.urlshortener.dto.DailyAnalyticsResponse;
import com.example.urlshortener.dto.TopAnalyticsResponse;
import com.example.urlshortener.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Redirect analytics ingestion and reporting")
@Validated
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a redirect event")
    public ClickAnalyticsResponse recordClick(@Valid @RequestBody ClickAnalyticsCreateRequest request) {
        return analyticsService.recordClick(request);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get analytics for a short code")
    public AnalyticsSummaryResponse getAnalytics(@PathVariable @Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$") String shortCode) {
        return analyticsService.getAnalytics(shortCode);
    }

    @GetMapping("/top")
    @Operation(summary = "Get most-clicked short codes")
    public List<TopAnalyticsResponse> getTopAnalytics(@RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return analyticsService.getTopAnalytics(limit);
    }

    @GetMapping("/daily")
    @Operation(summary = "Get daily click totals in UTC")
    public List<DailyAnalyticsResponse> getDailyAnalytics(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return analyticsService.getDailyAnalytics(from, to);
    }
}
