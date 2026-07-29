package com.example.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI apiGatewayOpenApi() {
        return new OpenAPI().info(new Info()
                .title("URL Shortener API Gateway")
                .version("v1")
                .description("Gateway API contract for the URL Shortener platform."));
    }
}
