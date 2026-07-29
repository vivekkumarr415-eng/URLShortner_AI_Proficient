package com.example.urlshortener.config;

import com.example.urlshortener.client.AnalyticsServiceException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class AnalyticsFeignConfiguration {

    @Bean
    Retryer analyticsRetryer() {
        return new Retryer.Default(100, 500, 3);
    }

    @Bean
    ErrorDecoder analyticsErrorDecoder() {
        return (methodKey, response) -> new AnalyticsServiceException(
                response.status(), "Analytics service returned HTTP " + response.status() + " for " + methodKey);
    }
}
