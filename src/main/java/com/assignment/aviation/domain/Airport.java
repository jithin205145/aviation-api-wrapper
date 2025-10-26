package com.assignment.aviation.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Airport(
        String icao,
        String iata,
        String name,
        String city,
        String state,
        String country,
        Double latitude,
        Double longitude,
        Integer elevationFt,
        String timezone
) {}

