package com.example.urlshortener.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A bounded, per-instance fixed-window limiter; production deployments should use a shared edge limiter. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final int requestsPerMinute;
    private final Clock clock;

    @Autowired
    public RateLimitFilter(@Value("${security.rate-limit.requests-per-minute:120}") int requestsPerMinute) {
        this(requestsPerMinute, Clock.systemUTC());
    }

    RateLimitFilter(int requestsPerMinute, Clock clock) {
        this.requestsPerMinute = requestsPerMinute;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/api/v1/urls") || path.startsWith("/r/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr() + ':' + request.getRequestURI().split("/", 4)[1];
        long now = clock.millis();
        Window window = windows.compute(key, (ignored, current) -> current == null || now - current.startedAt >= 60_000
                ? new Window(now, 1) : current.increment());
        if (window.requests > requestsPerMinute) {
            response.setHeader("Retry-After", "60");
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Rate limit exceeded");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private record Window(long startedAt, int requests) {
        Window increment() { return new Window(startedAt, requests + 1); }
    }
}
