package com.assignment.aviation.provider;

import com.assignment.aviation.provider.dto.AviationApiAirportResponse;
import reactor.core.publisher.Mono;

public interface AirportProvider {
    Mono<AviationApiAirportResponse> fetchByIcao(String icao);
}

