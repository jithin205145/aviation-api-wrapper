package com.assignment.aviation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration for cache settings.
 */
@ConfigurationProperties(prefix = "cache.airports")
public record CacheAirportsProperties(
        long maxSize,
        String ttl
) {}

