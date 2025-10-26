package com.assignment.aviation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration for aviation API settings.
 */
@ConfigurationProperties(prefix = "aviation.api")
public record AviationApiProperties(
        String baseUrl
) {}

