package com.bloghub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bloghub")
public record BloghubProperties(
        Api api,
        Security security
) {
    public record Api(String basePath) {}

    public record Security(Jwt jwt) {
        public record Jwt(String secret, long expirationMinutes) {}
    }
}

