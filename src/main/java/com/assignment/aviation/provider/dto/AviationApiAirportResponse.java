package com.assignment.aviation.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AviationApiAirportResponse(
        Map<String, List<UpstreamAirport>> airports
) {
}
