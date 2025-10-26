package com.assignment.aviation.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpstreamAirport(
        @JsonProperty("icao_ident") String icaoIdent,
        @JsonProperty("faa_ident") String faaIdent,
        @JsonProperty("facility_name") String facilityName,
        String city,
        String state,
        String latitude,
        String longitude,
        String elevation
) {}

