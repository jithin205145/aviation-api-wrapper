package com.assignment.aviation.service;

import com.assignment.aviation.domain.Airport;
import com.assignment.aviation.provider.AirportProvider;
import com.github.benmanes.caffeine.cache.AsyncCache;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

import com.assignment.aviation.exception.AirportNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AirportServiceImpl implements AirportService {
    private final AirportProvider provider;
    private final AsyncCache<String, Airport> cache;

    @Override
    @Retry(name = "airport-api")
    @CircuitBreaker(name = "airport-api")
    @Bulkhead(name = "airport-api")
    public Mono<Airport> getByIcao(String icao) {
        String norm = icao == null ? null : icao.trim().toUpperCase();
        if (norm == null || norm.isBlank()) {
            return Mono.error(new IllegalArgumentException("ICAO is required"));
        }

        Airport cached = cache.synchronous().getIfPresent(norm);
        if (cached != null) {
            log.debug("Cache hit for ICAO={}", norm);
            return Mono.just(cached);
        }

        return provider.fetchByIcao(norm)
                .doOnSubscribe(s -> log.info("Fetching airport from provider, icao={}", norm))
                .flatMap(r -> {
                    Airport a = AirportMapper.toDomain(r);
                    if (a == null) return Mono.empty();
                    // Populate async cache
                    cache.put(norm, CompletableFuture.completedFuture(a));
                    return Mono.just(a);
                })
                .switchIfEmpty(Mono.error(new AirportNotFoundException("Airport not found for ICAO=" + norm)));
    }
}
