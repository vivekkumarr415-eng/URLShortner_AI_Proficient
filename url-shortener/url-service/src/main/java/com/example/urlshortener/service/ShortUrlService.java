package com.example.urlshortener.service;

import com.example.urlshortener.dto.ShortUrlCreateRequest;
import com.example.urlshortener.dto.ShortUrlResponse;
import com.example.urlshortener.dto.ShortUrlUpdateRequest;
import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.exception.DuplicateAliasException;
import com.example.urlshortener.exception.ShortUrlExpiredException;
import com.example.urlshortener.exception.ShortUrlInactiveException;
import com.example.urlshortener.exception.ShortUrlNotFoundException;
import com.example.urlshortener.mapper.ShortUrlMapper;
import com.example.urlshortener.repository.ShortUrlRepository;
import com.example.urlshortener.util.ShortCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlMapper shortUrlMapper;
    private final ShortCodeGenerator shortCodeGenerator;
    private final Clock clock;

    public ShortUrlService(ShortUrlRepository shortUrlRepository, ShortUrlMapper shortUrlMapper,
                           ShortCodeGenerator shortCodeGenerator, Clock clock) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortUrlMapper = shortUrlMapper;
        this.shortCodeGenerator = shortCodeGenerator;
        this.clock = clock;
    }

    @Transactional
    public ShortUrlResponse create(ShortUrlCreateRequest request) {
        String customAlias = request.customAlias();
        if (customAlias != null && isCodeInUse(customAlias)) {
            throw new DuplicateAliasException(customAlias);
        }

        ShortUrl shortUrl = new ShortUrl(
                request.originalUrl(),
                generateAvailableShortCode(),
                customAlias,
                request.expiryDate(),
                true,
                0
        );
        return shortUrlMapper.toResponse(shortUrlRepository.save(shortUrl));
    }

    public ShortUrlResponse getByCode(String shortCode) {
        return shortUrlMapper.toResponse(findByCode(shortCode));
    }

    @Transactional
    public ShortUrlResponse update(Long id, ShortUrlUpdateRequest request) {
        ShortUrl shortUrl = findById(id);
        String customAlias = request.customAlias();
        if (customAlias != null && isAliasUsedByAnotherShortUrl(customAlias, id)) {
            throw new DuplicateAliasException(customAlias);
        }

        shortUrl.update(request.originalUrl(), customAlias, request.expiryDate(), request.active());
        return shortUrlMapper.toResponse(shortUrlRepository.save(shortUrl));
    }

    @Transactional
    public void delete(Long id) {
        shortUrlRepository.delete(findById(id));
    }

    @Transactional
    public String resolveDestination(String shortCode) {
        ShortUrl shortUrl = findByCode(shortCode);
        if (!shortUrl.isActive()) {
            throw new ShortUrlInactiveException(shortCode);
        }
        if (shortUrl.getExpiryDate() != null && !shortUrl.getExpiryDate().isAfter(Instant.now(clock))) {
            throw new ShortUrlExpiredException(shortCode);
        }

        shortUrl.registerClick();
        return shortUrl.getOriginalUrl();
    }

    private ShortUrl findById(Long id) {
        return shortUrlRepository.findById(id)
                .orElseThrow(() -> new ShortUrlNotFoundException(id.toString()));
    }

    private ShortUrl findByCode(String shortCode) {
        return shortUrlRepository.findByShortCodeOrCustomAlias(shortCode, shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }

    private boolean isAliasUsedByAnotherShortUrl(String alias, Long id) {
        return shortUrlRepository.findByShortCodeOrCustomAlias(alias, alias)
                .filter(existing -> !existing.getId().equals(id))
                .isPresent();
    }

    private boolean isCodeInUse(String code) {
        return shortUrlRepository.existsByShortCodeOrCustomAlias(code, code);
    }

    private String generateAvailableShortCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String candidate = shortCodeGenerator.generate();
            if (!isCodeInUse(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate a unique short code");
    }
}
