package com.example.urlshortener.service;

import com.example.urlshortener.client.AnalyticsServiceClient;
import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsEventPublisher {

    private final AnalyticsServiceClient analyticsServiceClient;

    public AnalyticsEventPublisher(AnalyticsServiceClient analyticsServiceClient) {
        this.analyticsServiceClient = analyticsServiceClient;
    }

    @Async
    public void record(ClickAnalyticsCreateRequest event) {
        analyticsServiceClient.recordClick(event);
    }
}
