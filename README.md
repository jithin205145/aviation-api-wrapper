# Aviation API Wrapper

A small Spring Boot service that fetches airport details by ICAO code from aviationapi.com and exposes a clean HTTP API.

## Quick Start

- Run locally
```bash
mvn spring-boot:run
```
- Build & test
```bash
mvn clean verify
```
- Docker (optional)
```bash
docker build -t aviation-api-wrapper:latest .
docker run --rm -p 8080:8080 aviation-api-wrapper:latest
```

## API

- GET `/api/airports/{icao}`
  - Path parameter: `icao` (4 alphanumeric, e.g., KJFK, EGLL)
  - Example
    ```bash
    curl http://localhost:8080/api/airports/KJFK
    ```
    Response (200):
    ```json
    {
      "icao": "KJFK",
      "iata": "JFK",
      "name": "JOHN F KENNEDY INTL",
      "city": "NEW YORK",
      "state": "NY",
      "latitude": 40.6399277778,
      "longitude": -73.7786925,
      "elevationFt": 13
    }
    ```
  - Errors
    - 400 BAD_REQUEST – invalid ICAO format
    - 404 NOT_FOUND – airport not found
    - 502 BAD_GATEWAY – upstream error/connection issue
    - 503 SERVICE_UNAVAILABLE – circuit breaker/bulkhead

## Swagger / OpenAPI
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml

## Actuator
- Base path: `/actuator`
- Health: `GET /actuator/health`
- Info: `GET /actuator/info`
- Prometheus metrics: `GET /actuator/prometheus`

## Architecture & Resilience (brief)
- Reactive stack: Spring WebFlux + WebClient
- Provider abstraction: easy to swap aviation data sources
- Resilience (Resilience4j): retry, circuit breaker, bulkhead
- Caching: Caffeine async cache for recent lookups
- Observability: Actuator health and Prometheus metrics

## Notes: Assumptions, Architecture Decisions, Error Handling
- Assumptions
  - Only ICAO lookup is in scope; upstream may be unstable
  - No authentication or user management
  - Response is normalized to a compact airport model
- Architecture decisions
  - Stateless service, horizontally scalable
  - Clear layering: Controller → Service → Provider
  - Resilience via Resilience4j (decorators on service layer)
  - Async caching with Caffeine to reduce upstream load
  - Metrics/health via Spring Boot Actuator + Micrometer
- Error handling (centralized)
  - 400: validation/illegal arguments
  - 404: airport not found
  - 502: upstream HTTP/connection errors
  - 503: circuit breaker open or bulkhead saturated
  - 504: request timeout
  - 500: unexpected errors

## Future Enhancements
- Per-customer rate limiting (gateway policy or Bucket4j + Redis)
- Distributed cache (Redis) for multi-instance deployments
- Request tracing/correlation IDs and richer logs
- Additional fields and examples in OpenAPI docs
- More integration tests (half-open recovery, timeouts, bulkhead)

## AI Usage

This project was scaffolded with AI assistance for:
- Boilerplate code (Spring Boot setup, Maven,Swagger, Actuator info config)
- Integration test structure (WireMock setup)
- Dockerfile multi-stage build
