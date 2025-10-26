package com.assignment.aviation.config;

import com.assignment.aviation.domain.Airport;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder,
                               AviationApiProperties apiProps,
                               HttpClientProperties httpProps) {
        ConnectionProvider cp = ConnectionProvider.builder("aviation-http")
                .maxConnections(200)
                .pendingAcquireMaxCount(1000)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .build();

        HttpClient httpClient = HttpClient.create(cp)
                .followRedirect(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, httpProps.connectTimeoutMs())
                .responseTimeout(Duration.ofMillis(httpProps.readTimeoutMs()));

        return builder
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .baseUrl(apiProps.baseUrl())
                .build();
    }

    @Bean
    public AsyncCache<String, Airport> airportsAsyncCache(CacheAirportsProperties cacheProps) {
        Duration ttl = Duration.parse(cacheProps.ttl());
        return Caffeine.newBuilder()
                .maximumSize(cacheProps.maxSize())
                .expireAfterWrite(ttl)
                .buildAsync();
    }
}
