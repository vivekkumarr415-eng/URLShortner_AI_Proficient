package com.example.urlshortener.dto;

import java.time.LocalDate;

public record DailyAnalyticsResponse(LocalDate date, long totalClicks) {
}
