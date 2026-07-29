package com.example.urlshortener.mapper;

import com.example.urlshortener.dto.ShortUrlResponse;
import com.example.urlshortener.entity.ShortUrl;
import org.springframework.stereotype.Component;

@Component
public class ShortUrlMapper {

    public ShortUrlResponse toResponse(ShortUrl shortUrl) {
        String publicCode = shortUrl.getCustomAlias() == null ? shortUrl.getShortCode() : shortUrl.getCustomAlias();
        return new ShortUrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrl.getCustomAlias(),
                publicCode,
                shortUrl.getCreatedAt(),
                shortUrl.getExpiryDate(),
                shortUrl.isActive(),
                shortUrl.getClickCount()
        );
    }
}
