package com.assignment.aviation.service;

import com.assignment.aviation.domain.Airport;
import reactor.core.publisher.Mono;

public interface AirportService {
    Mono<Airport> getByIcao(String icao);
}

