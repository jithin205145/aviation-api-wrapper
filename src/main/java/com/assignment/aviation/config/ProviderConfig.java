package com.assignment.aviation.config;

import com.assignment.aviation.provider.AirportProvider;
import com.assignment.aviation.provider.AviationApiProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ProviderConfig {

    @Bean
    public AirportProvider airportProvider(WebClient webClient) {
        return new AviationApiProvider(webClient);
    }
}

