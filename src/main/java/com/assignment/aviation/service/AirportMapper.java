package com.assignment.aviation.service;

import com.assignment.aviation.domain.Airport;
import com.assignment.aviation.provider.dto.AviationApiAirportResponse;
import com.assignment.aviation.provider.dto.UpstreamAirport;

import java.util.List;
import java.util.Map;

/**
 * Mapper to convert provider DTOs to domain models.
 * Decouples service layer from provider-specific schemas.
 */
public final class AirportMapper {
    private AirportMapper() {
        // Utility class
    }

    /**
     * Map provider response to domain Airport model.
     *
     * @param r Provider response
     * @return Domain Airport or null if invalid
     */
    public static Airport toDomain(AviationApiAirportResponse r) {
        if (r == null || r.airports() == null || r.airports().isEmpty()) {
            return null;
        }
        // Prefer exact ICAO key if present
        UpstreamAirport u = null;
        for (Map.Entry<String, List<UpstreamAirport>> e : r.airports().entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                u = e.getValue().get(0);
                break;
            }
        }
        if (u == null) return null;
        Double lat = parseDms(u.latitude());
        Double lon = parseDms(u.longitude());
        Integer elev = parseInt(u.elevation());
        return new Airport(
                u.icaoIdent(),
                u.faaIdent(),
                u.facilityName(),
                u.city(),
                u.state(),
                null,
                lat,
                lon,
                elev,
                null
        );
    }

    // Parses DMS like 40-38-23.7400N to decimal degrees
    private static Double parseDms(String dms) {
        if (dms == null || dms.isBlank()) return null;
        try {
            // Example: 40-38-23.7400N or 073-46-43.2930W
            String s = dms.trim();
            boolean neg = s.endsWith("S") || s.endsWith("W");
            s = s.substring(0, s.length() - 1); // drop N/S/E/W
            String[] parts = s.split("-");
            if (parts.length != 3) return null;
            double deg = Double.parseDouble(parts[0]);
            double min = Double.parseDouble(parts[1]);
            double sec = Double.parseDouble(parts[2]);
            double val = deg + (min / 60.0) + (sec / 3600.0);
            return neg ? -val : val;
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
