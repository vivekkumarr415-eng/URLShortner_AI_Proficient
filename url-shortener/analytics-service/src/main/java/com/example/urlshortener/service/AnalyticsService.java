package com.example.urlshortener.service;

import com.example.urlshortener.dto.AnalyticsSummaryResponse;
import com.example.urlshortener.dto.ClickAnalyticsCreateRequest;
import com.example.urlshortener.dto.ClickAnalyticsResponse;
import com.example.urlshortener.dto.DailyAnalyticsResponse;
import com.example.urlshortener.dto.TopAnalyticsResponse;
import com.example.urlshortener.entity.ClickAnalytics;
import com.example.urlshortener.mapper.ClickAnalyticsMapper;
import com.example.urlshortener.repository.ClickAnalyticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ClickAnalyticsRepository repository;
    private final ClickAnalyticsMapper mapper;
    private final Clock clock;

    public AnalyticsService(ClickAnalyticsRepository repository, ClickAnalyticsMapper mapper, Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional
    public ClickAnalyticsResponse recordClick(ClickAnalyticsCreateRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    public AnalyticsSummaryResponse getAnalytics(String shortCode) {
        List<ClickAnalytics> clicks = repository.findByShortCodeOrderByClickedAtAsc(shortCode);
        return new AnalyticsSummaryResponse(shortCode, clicks.size(), countBy(clicks, ClickAnalytics::getBrowser),
                countBy(clicks, ClickAnalytics::getDevice), countBy(clicks, ClickAnalytics::getOperatingSystem),
                countBy(clicks, event -> event.getReferrer() == null || event.getReferrer().isBlank() ? "Direct" : event.getReferrer()));
    }

    public List<TopAnalyticsResponse> getTopAnalytics(int limit) {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(ClickAnalytics::getShortCode, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(entry -> new TopAnalyticsResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<DailyAnalyticsResponse> getDailyAnalytics(LocalDate from, LocalDate to) {
        LocalDate end = to == null ? LocalDate.now(clock.withZone(ZoneOffset.UTC)) : to;
        LocalDate start = from == null ? end.minusDays(29) : from;
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
        Instant startInstant = start.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endExclusive = end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Map<LocalDate, Long> counts = repository.findByClickedAtGreaterThanEqualAndClickedAtLessThanOrderByClickedAtAsc(startInstant, endExclusive)
                .stream()
                .collect(Collectors.groupingBy(event -> event.getClickedAt().atZone(ZoneOffset.UTC).toLocalDate(), TreeMap::new, Collectors.counting()));
        return start.datesUntil(end.plusDays(1))
                .map(date -> new DailyAnalyticsResponse(date, counts.getOrDefault(date, 0L)))
                .toList();
    }

    private Map<String, Long> countBy(List<ClickAnalytics> clicks, Function<ClickAnalytics, String> classifier) {
        return clicks.stream().collect(Collectors.groupingBy(classifier, TreeMap::new, Collectors.counting()));
    }
}
