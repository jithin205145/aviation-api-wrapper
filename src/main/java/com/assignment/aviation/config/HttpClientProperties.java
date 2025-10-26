package com.assignment.aviation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "http.client")
public record HttpClientProperties(
        int connectTimeoutMs,
        int readTimeoutMs
) {}

