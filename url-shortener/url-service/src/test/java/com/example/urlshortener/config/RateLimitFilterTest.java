package com.example.urlshortener.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    @Test
    void rejectsRequestAfterFixedWindowLimitIsReached() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(1, java.time.Clock.systemUTC());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/r/abc");
        request.setRemoteAddr("192.0.2.10");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        MockHttpServletResponse rejected = new MockHttpServletResponse();
        filter.doFilter(request, rejected, new MockFilterChain());

        assertThat(rejected.getStatus()).isEqualTo(429);
        assertThat(rejected.getHeader("Retry-After")).isEqualTo("60");
    }
}
