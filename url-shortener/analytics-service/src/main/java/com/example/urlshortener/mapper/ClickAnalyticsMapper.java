package com.example.urlshortener.mapper;

import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import com.example.urlshortener.dto.ClickAnalyticsResponse;
import com.example.urlshortener.entity.ClickAnalytics;
import org.springframework.stereotype.Component;

@Component
public class ClickAnalyticsMapper {

    public ClickAnalytics toEntity(ClickAnalyticsCreateRequest request) {
        return new ClickAnalytics(request.shortCode(), request.clickedAt(), request.ipAddress(), request.browser(),
                request.device(), request.operatingSystem(), request.referrer());
    }

    public ClickAnalyticsResponse toResponse(ClickAnalytics analytics) {
        return new ClickAnalyticsResponse(analytics.getId(), analytics.getShortCode(), analytics.getClickedAt(),
                analytics.getIpAddress(), analytics.getBrowser(), analytics.getDevice(),
                analytics.getOperatingSystem(), analytics.getReferrer());
    }
}
