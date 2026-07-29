package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ClickAnalytics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClickAnalyticsRepositoryTest {

    @Autowired
    private ClickAnalyticsRepository clickAnalyticsRepository;

    @Test
    void findsClicksForShortCodeWithinTimeRangeInAscendingOrder() {
        Instant firstClick = Instant.now().minusSeconds(30);
        clickAnalyticsRepository.saveAndFlush(new ClickAnalytics(
                "abc123xy", firstClick, "203.0.113.10", "Chrome", "Desktop", "Linux", "https://example.com"
        ));
        clickAnalyticsRepository.saveAndFlush(new ClickAnalytics(
                "other-code", Instant.now().minusSeconds(20), "203.0.113.11", "Firefox", "Desktop", "Linux", null
        ));

        assertThat(clickAnalyticsRepository.findByShortCodeAndClickedAtBetweenOrderByClickedAtAsc(
                "abc123xy", Instant.now().minusSeconds(60), Instant.now()))
                .extracting(ClickAnalytics::getShortCode, ClickAnalytics::getClickedAt)
                .containsExactly(tuple("abc123xy", firstClick));
    }

    private static org.assertj.core.groups.Tuple tuple(Object... values) {
        return org.assertj.core.groups.Tuple.tuple(values);
    }
}
