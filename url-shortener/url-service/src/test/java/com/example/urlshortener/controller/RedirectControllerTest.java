package com.example.urlshortener.controller;

import com.example.urlshortener.service.AnalyticsEventPublisher;
import com.example.urlshortener.service.ShortUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    @Mock
    private ShortUrlService shortUrlService;

    @Mock
    private AnalyticsEventPublisher analyticsEventPublisher;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RedirectController(shortUrlService, analyticsEventPublisher)).build();
    }

    @Test
    void redirectsToResolvedDestination() throws Exception {
        when(shortUrlService.resolveDestination("products")).thenReturn("https://example.com/products");

        mockMvc.perform(get("/r/products"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/products"));

        verify(analyticsEventPublisher).record(any());
    }
}
