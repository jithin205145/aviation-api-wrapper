package com.assignment.aviation;

import com.assignment.aviation.config.AviationApiProperties;
import com.assignment.aviation.config.CacheAirportsProperties;
import com.assignment.aviation.config.HttpClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties({AviationApiProperties.class, HttpClientProperties.class, CacheAirportsProperties.class})
public class AviationApiWrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(AviationApiWrapperApplication.class, args);
    }
}
