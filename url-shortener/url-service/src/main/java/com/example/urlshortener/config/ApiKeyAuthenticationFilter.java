package com.example.urlshortener.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Protects link-management APIs while keeping public redirect URLs accessible. */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final byte[] expectedApiKey;

    public ApiKeyAuthenticationFilter(@Value("${security.api-key:}") String apiKey) {
        this.expectedApiKey = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/urls");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        byte[] suppliedApiKey = String.valueOf(request.getHeader(HttpHeaders.AUTHORIZATION))
                .replaceFirst("(?i)^ApiKey\\s+", "")
                .getBytes(StandardCharsets.UTF_8);
        if (expectedApiKey.length == 0 || !MessageDigest.isEqual(expectedApiKey, suppliedApiKey)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "A valid API key is required");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
