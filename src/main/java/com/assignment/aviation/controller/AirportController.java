package com.assignment.aviation.controller;

import com.assignment.aviation.domain.Airport;
import com.assignment.aviation.service.AirportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/airports")
@Validated
@Tag(name = "Airports", description = "Airport lookup by ICAO code")
@RequiredArgsConstructor
public class AirportController {
    private final AirportService airportService;

    @Operation(
            summary = "Get airport by ICAO",
            description = "Returns normalized airport information for the given 4-character ICAO code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = Airport.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid ICAO format"),
                    @ApiResponse(responseCode = "404", description = "Airport not found"),
                    @ApiResponse(responseCode = "502", description = "Upstream error"),
                    @ApiResponse(responseCode = "503", description = "Service unavailable due to circuit breaker/bulkhead")
            }
    )
    @GetMapping("/{icao}")
    public Mono<Airport> getAirport(@PathVariable("icao")
                                    @Parameter(name = "icao", in = ParameterIn.PATH, required = true,
                                            description = "ICAO code, exactly 4 alphanumeric characters", example = "KJFK")
                                    @Pattern(regexp = "^[A-Za-z0-9]{4}$", message = "ICAO must be exactly 4 alphanumeric characters")
                                    String icao) {
        return airportService.getByIcao(icao);
    }
}
