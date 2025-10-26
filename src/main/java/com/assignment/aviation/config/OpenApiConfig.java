package com.assignment.aviation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aviationApiOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Aviation API Wrapper")
                        .description("Backend microservice that wraps aviationapi.com to fetch airport details by ICAO code.")
                        .version("0.1.0")
                        .license(new License().name("Proprietary"))
                        .contact(new Contact().name("Aviation API Wrapper")))
                .externalDocs(new ExternalDocumentation()
                        .description("Aviation API")
                        .url("https://aviationapi.com/"));
    }
}

