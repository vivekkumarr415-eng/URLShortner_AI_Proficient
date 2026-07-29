package com.example.urlshortener.dto;

import java.util.Map;

public record AnalyticsSummaryResponse(
        String shortCode,
        long totalClicks,
        Map<String, Long> browsers,
        Map<String, Long> devices,
        Map<String, Long> operatingSystems,
        Map<String, Long> referrers
) {
}
