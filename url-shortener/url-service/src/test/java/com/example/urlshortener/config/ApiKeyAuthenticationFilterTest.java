package com.example.urlshortener.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthenticationFilterTest {

    @Test
    void rejectsManagementRequestWithoutValidApiKey() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("test-key");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/urls");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void allowsManagementRequestWithApiKeyScheme() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter("test-key");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/urls");
        request.addHeader("Authorization", "ApiKey test-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }
}
