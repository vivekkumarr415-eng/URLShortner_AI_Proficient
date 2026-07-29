package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortUrlCreateRequest;
import com.example.urlshortener.dto.ShortUrlResponse;
import com.example.urlshortener.dto.ShortUrlUpdateRequest;
import com.example.urlshortener.exception.ApiError;
import com.example.urlshortener.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/urls")
@Tag(name = "Short URLs", description = "Create and manage short URLs")
@Validated
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping
    @Operation(summary = "Create a short URL")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short URL created"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Custom alias already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody ShortUrlCreateRequest request) {
        ShortUrlResponse response = shortUrlService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{shortCode}")
                .buildAndExpand(response.publicCode())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get short URL metadata by short code or custom alias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Short URL found"),
            @ApiResponse(responseCode = "404", description = "Short URL not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ShortUrlResponse getByCode(@PathVariable @Pattern(regexp = "^[A-Za-z0-9_-]{3,32}$") String shortCode) {
        return shortUrlService.getByCode(shortCode);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a short URL and its active state")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Short URL updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Short URL not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Custom alias already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ShortUrlResponse update(@PathVariable Long id, @Valid @RequestBody ShortUrlUpdateRequest request) {
        return shortUrlService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a short URL")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Short URL deleted"),
            @ApiResponse(responseCode = "404", description = "Short URL not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shortUrlService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
