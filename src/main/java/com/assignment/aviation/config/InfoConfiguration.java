package com.assignment.aviation.config;

import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class InfoConfiguration {

    @Bean
    public InfoContributor runtimeInfoContributor(AviationApiProperties apiProps,
                                                  HttpClientProperties httpProps,
                                                  CacheAirportsProperties cacheProps) {
        return builder -> {
            Map<String, Object> runtime = new LinkedHashMap<>();
            runtime.put("aviationApi.baseUrl", apiProps.baseUrl());
            runtime.put("http.connectTimeoutMs", httpProps.connectTimeoutMs());
            runtime.put("http.readTimeoutMs", httpProps.readTimeoutMs());
            runtime.put("cache.airports.maxSize", cacheProps.maxSize());
            runtime.put("cache.airports.ttl", cacheProps.ttl());
            builder.withDetail("runtime", runtime);
        };
    }
}
