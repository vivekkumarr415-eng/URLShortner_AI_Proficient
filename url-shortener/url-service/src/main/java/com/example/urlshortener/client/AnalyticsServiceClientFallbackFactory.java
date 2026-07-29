package com.example.urlshortener.client;

import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsServiceClientFallbackFactory implements FallbackFactory<AnalyticsServiceClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsServiceClientFallbackFactory.class);

    @Override
    public AnalyticsServiceClient create(Throwable cause) {
        return new AnalyticsServiceClient() {
            @Override
            public void recordClick(ClickAnalyticsCreateRequest event) {
                LOGGER.warn("Analytics event was not delivered for shortCode={}; redirect remains successful", event.shortCode(), cause);
            }
        };
    }
}
