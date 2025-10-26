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
@ContextConfiguration(initializers = AirportControllerIT.Initializer.class)
class AirportControllerIT {

    static WireMockServer wireMockServer;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            wireMockServer = new WireMockServer(0);
            wireMockServer.start();
            String baseUrl = "http://localhost:" + wireMockServer.port();
            TestPropertyValues.of(
                    "aviation.api.base-url=" + baseUrl
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
    void shouldReturnAirportByIcao() {
        stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("icao", equalTo("KJFK"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\n  \"icao\": \"KJFK\", \"iata\": \"JFK\", \"name\": \"John F. Kennedy International Airport\", \"city\": \"New York\", \"state\": \"NY\", \"country\": \"US\", \"latitude\": 40.6413, \"longitude\": -73.7781, \"elevation\": 13, \"timezone\": \"America/New_York\"\n}")));

        webTestClient.get()
                .uri("/api/airports/{icao}", "KJFK")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.icao").isEqualTo("KJFK")
                .jsonPath("$.iata").isEqualTo("JFK")
                .jsonPath("$.name").isEqualTo("John F. Kennedy International Airport");
    }

    @Test
    void shouldHandleNotFound() {
        stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("icao", equalTo("XXXX"))
                .willReturn(aResponse().withStatus(404)));

        webTestClient.get()
                .uri("/api/airports/{icao}", "XXXX")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_GATEWAY); // mapped as upstream error
    }

    @Test
    void shouldValidateIcao() {
        webTestClient.get()
                .uri("/api/airports/{icao}", "TOO_LONG_CODE")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldServeFromCacheWhenUpstreamUnavailable() {
        // First call populates cache
        stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("icao", equalTo("KSEA"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\n  \"icao\": \"KSEA\", \"iata\": \"SEA\", \"name\": \"Seattle-Tacoma International Airport\", \"city\": \"Seattle\", \"state\": \"WA\", \"country\": \"US\", \"latitude\": 47.4502, \"longitude\": -122.3088, \"elevation\": 433, \"timezone\": \"America/Los_Angeles\"\n}")));

        webTestClient.get().uri("/api/airports/{icao}", "KSEA").exchange().expectStatus().isOk();

        // Upstream now unavailable; cache should still serve
        wireMockServer.resetAll();
        stubFor(get(urlPathEqualTo("/v1/airports")).withQueryParam("icao", equalTo("KSEA")).willReturn(aResponse().withStatus(500)));

        webTestClient.get().uri("/api/airports/{icao}", "KSEA")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.icao").isEqualTo("KSEA");
    }
}
