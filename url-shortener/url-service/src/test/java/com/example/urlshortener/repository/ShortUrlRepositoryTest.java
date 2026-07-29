package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ShortUrl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShortUrlRepositoryTest {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Test
    void savesAndFindsShortUrlByShortCode() {
        ShortUrl shortUrl = new ShortUrl(
                "https://example.com/articles/url-shortening",
                "abc123xy",
                "example-link",
                Instant.now().plusSeconds(3_600),
                true,
                0
        );

        shortUrlRepository.saveAndFlush(shortUrl);

        assertThat(shortUrl.getId()).isNotNull();
        assertThat(shortUrlRepository.findByShortCode("abc123xy"))
                .isPresent()
                .get()
                .extracting(ShortUrl::getOriginalUrl, ShortUrl::getCustomAlias, ShortUrl::isActive)
                .containsExactly("https://example.com/articles/url-shortening", "example-link", true);
        assertThat(shortUrlRepository.existsByCustomAlias("example-link")).isTrue();
    }
}
