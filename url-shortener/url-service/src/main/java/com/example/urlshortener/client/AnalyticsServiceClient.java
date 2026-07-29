package com.example.urlshortener.client;

import com.example.urlshortener.config.AnalyticsFeignConfiguration;
import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "analytics-service",
        url = "${analytics-service.base-url:http://localhost:8082}",
        configuration = AnalyticsFeignConfiguration.class,
        fallbackFactory = AnalyticsServiceClientFallbackFactory.class
)
public interface AnalyticsServiceClient {

    @PostMapping("/analytics/events")
    void recordClick(@RequestBody ClickAnalyticsCreateRequest event);
}
