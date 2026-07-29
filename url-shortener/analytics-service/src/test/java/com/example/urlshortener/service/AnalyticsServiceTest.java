package com.example.urlshortener.service;

import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import com.example.urlshortener.dto.DailyAnalyticsResponse;
import com.example.urlshortener.dto.TopAnalyticsResponse;
import com.example.urlshortener.entity.ClickAnalytics;
import com.example.urlshortener.mapper.ClickAnalyticsMapper;
import com.example.urlshortener.repository.ClickAnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ClickAnalyticsRepository repository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(repository, new ClickAnalyticsMapper(),
                Clock.fixed(Instant.parse("2026-07-29T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void recordsRedirectEvents() {
        ClickAnalytics event = new ClickAnalytics("products", Instant.parse("2026-07-29T11:00:00Z"), "203.0.113.2",
                "Chrome", "Desktop", "Linux", "https://example.com");
        when(repository.save(any(ClickAnalytics.class))).thenReturn(event);

        analyticsService.recordClick(new ClickAnalyticsCreateRequest("products", event.getClickedAt(), "203.0.113.2",
                "Chrome", "Desktop", "Linux", "https://example.com"));

        ArgumentCaptor<ClickAnalytics> captor = ArgumentCaptor.forClass(ClickAnalytics.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getShortCode()).isEqualTo("products");
    }

    @Test
    void aggregatesTopAndDailyClicks() {
        Instant first = Instant.parse("2026-07-28T10:00:00Z");
        Instant second = Instant.parse("2026-07-29T10:00:00Z");
        ClickAnalytics products = new ClickAnalytics("products", first, "203.0.113.2", "Chrome", "Desktop", "Linux", null);
        ClickAnalytics docs = new ClickAnalytics("docs", second, "203.0.113.3", "Firefox", "Mobile", "Android", null);
        when(repository.findAll()).thenReturn(List.of(products, products, docs));
        when(repository.findByClickedAtGreaterThanEqualAndClickedAtLessThanOrderByClickedAtAsc(any(), any()))
                .thenReturn(List.of(products, docs));

        List<TopAnalyticsResponse> top = analyticsService.getTopAnalytics(10);
        List<DailyAnalyticsResponse> daily = analyticsService.getDailyAnalytics(LocalDate.parse("2026-07-28"), LocalDate.parse("2026-07-29"));

        assertThat(top).containsExactly(new TopAnalyticsResponse("products", 2), new TopAnalyticsResponse("docs", 1));
        assertThat(daily).containsExactly(new DailyAnalyticsResponse(LocalDate.parse("2026-07-28"), 1),
                new DailyAnalyticsResponse(LocalDate.parse("2026-07-29"), 1));
    }
}
