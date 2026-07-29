package com.example.urlshortener.service;

import com.example.urlshortener.client.AnalyticsServiceClient;
import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventPublisherTest {

    @Mock
    private AnalyticsServiceClient analyticsServiceClient;

    @Test
    void sendsRedirectEventToAnalyticsService() {
        AnalyticsEventPublisher publisher = new AnalyticsEventPublisher(analyticsServiceClient);
        ClickAnalyticsCreateRequest event = new ClickAnalyticsCreateRequest("products", Instant.now(), "203.0.113.10",
                "Chrome", "Desktop", "Linux", "https://example.com");

        publisher.record(event);

        verify(analyticsServiceClient).recordClick(event);
    }
}
