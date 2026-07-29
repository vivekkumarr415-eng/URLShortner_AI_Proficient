package com.example.urlshortener.client;

import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;

class AnalyticsServiceClientFallbackFactoryTest {

    @Test
    void fallbackAbsorbsAnalyticsFailure() {
        AnalyticsServiceClient client = new AnalyticsServiceClientFallbackFactory().create(new IllegalStateException("unavailable"));

        assertThatCode(() -> client.recordClick(new ClickAnalyticsCreateRequest("products", Instant.now(), "203.0.113.10",
                "Chrome", "Desktop", "Linux", null))).doesNotThrowAnyException();
    }
}
