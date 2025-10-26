package com.assignment.aviation.provider;

import com.assignment.aviation.exception.UpstreamServiceException;
import com.assignment.aviation.provider.dto.AviationApiAirportResponse;
import com.assignment.aviation.provider.dto.UpstreamAirport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class AviationApiProvider implements AirportProvider {
    private final WebClient webClient;

    @Override
    public Mono<AviationApiAirportResponse> fetchByIcao(String icao) {
        String norm = normalizeIcao(icao);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/airports")
                        .queryParam("apt", norm)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is3xxRedirection, resp -> resp.createException().flatMap(Mono::error))
                .onStatus(HttpStatusCode::is4xxClientError, resp -> resp.createException().flatMap(Mono::error))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        resp.createException().flatMap(ex -> Mono.error(new UpstreamServiceException("Upstream service error: " + resp.statusCode(), ex)))
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<UpstreamAirport>>>() {})
                .map(AviationApiAirportResponse::new)
                .onErrorMap(WebClientResponseException.NotFound.class, ex -> {
                    log.debug("Airport not found in upstream: icao={}", norm);
                    return ex;
                });
    }

    private String normalizeIcao(String icao) {
        return icao == null ? null : icao.trim().toUpperCase();
    }
}
