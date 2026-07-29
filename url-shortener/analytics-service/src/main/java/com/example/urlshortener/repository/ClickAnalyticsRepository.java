package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {

    List<ClickAnalytics> findByShortCodeAndClickedAtBetweenOrderByClickedAtAsc(String shortCode, Instant from, Instant to);

    List<ClickAnalytics> findByShortCodeOrderByClickedAtAsc(String shortCode);

    List<ClickAnalytics> findByClickedAtGreaterThanEqualAndClickedAtLessThanOrderByClickedAtAsc(Instant from, Instant to);
}
