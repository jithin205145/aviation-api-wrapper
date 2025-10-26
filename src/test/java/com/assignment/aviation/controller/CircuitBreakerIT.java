package com.assignment.aviation.controller;

import com.assignment.aviation.AviationApiWrapperApplication;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AviationApiWrapperApplication.class)
@ContextConfiguration(initializers = CircuitBreakerIT.Initializer.class)
class CircuitBreakerIT {

    static WireMockServer wireMockServer;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            wireMockServer = new WireMockServer(0);
            wireMockServer.start();
            String baseUrl = "http://localhost:" + wireMockServer.port();
            TestPropertyValues.of(
                    "aviation.api.base-url=" + baseUrl,
                    // Tighten the CB config to trip quickly
                    "resilience4j.circuitbreaker.instances.airport-api.sliding-window-type=COUNT_BASED",
                    "resilience4j.circuitbreaker.instances.airport-api.sliding-window-size=4",
                    "resilience4j.circuitbreaker.instances.airport-api.minimum-number-of-calls=4",
                    "resilience4j.circuitbreaker.instances.airport-api.failure-rate-threshold=50",
                    "resilience4j.circuitbreaker.instances.airport-api.wait-duration-in-open-state=2s",
                    // Disable retry for determinism (1 attempt per call)
                    "resilience4j.retry.instances.airport-api.max-attempts=1"
            ).applyTo(ctx.getEnvironment());
        }
    }

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        configureFor("localhost", wireMockServer.port());
        wireMockServer.resetAll();
    }

    @AfterAll
    static void tearDown() {
        if (wireMockServer != null) wireMockServer.stop();
    }

    @Test
    void circuitBreakerShouldOpenAfterConsecutiveFailuresAndShortCircuitCalls() {
        // Stub upstream to always return 500
        stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("icao", equalTo("KCBR"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"error\":\"boom\"}")));

        // Make 4 failing calls to reach the minimum number of calls and exceed failure threshold
        for (int i = 0; i < 4; i++) {
            webTestClient.get()
                    .uri("/api/airports/{icao}", "KCBR")
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.BAD_GATEWAY); // mapped from UpstreamServiceException
        }

        // Next call should be short-circuited by CircuitBreaker (CallNotPermittedException -> 503)
        webTestClient.get()
                .uri("/api/airports/{icao}", "KCBR")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        // Verify upstream was called only 4 times (the 5th should be short-circuited)
        verify(4, getRequestedFor(urlPathEqualTo("/v1/airports"))
                .withQueryParam("icao", equalTo("KCBR")));
    }
}
