package com.example.urlshortener.service;

import com.example.urlshortener.dto.ShortUrlCreateRequest;
import com.example.urlshortener.dto.ShortUrlUpdateRequest;
import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.exception.DuplicateAliasException;
import com.example.urlshortener.exception.ShortUrlExpiredException;
import com.example.urlshortener.exception.ShortUrlInactiveException;
import com.example.urlshortener.mapper.ShortUrlMapper;
import com.example.urlshortener.repository.ShortUrlRepository;
import com.example.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    private ShortUrlService shortUrlService;

    @BeforeEach
    void setUp() {
        shortUrlService = new ShortUrlService(
                shortUrlRepository,
                new ShortUrlMapper(),
                shortCodeGenerator,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void createsShortUrlWithGeneratedCode() {
        ShortUrlCreateRequest request = new ShortUrlCreateRequest(
                "https://example.com/products",
                "products",
                NOW.plusSeconds(3_600)
        );
        when(shortUrlRepository.existsByShortCodeOrCustomAlias("a1B2c3D4", "a1B2c3D4")).thenReturn(false);
        when(shortCodeGenerator.generate()).thenReturn("a1B2c3D4");
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = shortUrlService.create(request);

        assertThat(response.shortCode()).isEqualTo("a1B2c3D4");
        assertThat(response.publicCode()).isEqualTo("products");
        assertThat(response.active()).isTrue();
        assertThat(response.clickCount()).isZero();
    }

    @Test
    void rejectsDuplicateCustomAlias() {
        ShortUrlCreateRequest request = new ShortUrlCreateRequest(
                "https://example.com/products",
                "products",
                NOW.plusSeconds(3_600)
        );
        when(shortUrlRepository.existsByShortCodeOrCustomAlias("products", "products")).thenReturn(true);

        assertThatThrownBy(() -> shortUrlService.create(request))
                .isInstanceOf(DuplicateAliasException.class)
                .hasMessageContaining("products");
    }

    @Test
    void rejectsInactiveShortUrlDuringRedirectResolution() {
        ShortUrl shortUrl = new ShortUrl("https://example.com", "a1B2c3D4", null, null, false, 0);
        when(shortUrlRepository.findByShortCodeOrCustomAlias(anyString(), anyString())).thenReturn(Optional.of(shortUrl));

        assertThatThrownBy(() -> shortUrlService.resolveDestination("a1B2c3D4"))
                .isInstanceOf(ShortUrlInactiveException.class);
    }

    @Test
    void rejectsExpiredShortUrlDuringRedirectResolution() {
        ShortUrl shortUrl = new ShortUrl("https://example.com", "a1B2c3D4", null, NOW.minusSeconds(1), true, 0);
        when(shortUrlRepository.findByShortCodeOrCustomAlias(anyString(), anyString())).thenReturn(Optional.of(shortUrl));

        assertThatThrownBy(() -> shortUrlService.resolveDestination("a1B2c3D4"))
                .isInstanceOf(ShortUrlExpiredException.class);
    }

    @Test
    void updatesActiveStateAndDestination() {
        ShortUrl shortUrl = new ShortUrl("https://example.com", "a1B2c3D4", null, null, true, 0);
        ShortUrlUpdateRequest request = new ShortUrlUpdateRequest(
                "https://example.org",
                null,
                NOW.plusSeconds(3_600),
                false
        );
        when(shortUrlRepository.findById(1L)).thenReturn(Optional.of(shortUrl));
        when(shortUrlRepository.save(shortUrl)).thenReturn(shortUrl);

        var response = shortUrlService.update(1L, request);

        assertThat(response.originalUrl()).isEqualTo("https://example.org");
        assertThat(response.active()).isFalse();
    }
}
