package com.assignment.aviation.exception;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.util.concurrent.TimeoutException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AirportNotFoundException.class)
    public ErrorResponse handleNotFound(AirportNotFoundException ex) {
        log.debug("Airport not found: {}", ex.getMessage());
        return new ErrorResponse("NOT_FOUND", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ErrorResponse handleNoResource(NoResourceFoundException ex) {
        log.trace("Static resource not found: {}", ex.getMessage());
        return new ErrorResponse("NOT_FOUND", "Resource not found");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ErrorResponse handleValidation(Exception ex) {
        log.debug("Validation error: {}", ex.getMessage());
        return new ErrorResponse("BAD_REQUEST", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(CallNotPermittedException.class)
    public ErrorResponse handleCircuitBreakerOpen(CallNotPermittedException ex) {
        log.warn("Circuit breaker open: {}", ex.getMessage());
        return new ErrorResponse("SERVICE_UNAVAILABLE", "Service temporarily unavailable, please try again later");
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(BulkheadFullException.class)
    public ErrorResponse handleBulkheadFull(BulkheadFullException ex) {
        log.warn("Bulkhead full: {}", ex.getMessage());
        return new ErrorResponse("SERVICE_BUSY", "Service is busy, please try again later");
    }

    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    @ExceptionHandler(TimeoutException.class)
    public ErrorResponse handleTimeout(TimeoutException ex) {
        log.warn("Request timeout: {}", ex.getMessage());
        return new ErrorResponse("TIMEOUT", "Request timed out");
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(WebClientResponseException.class)
    public ErrorResponse handleWebClientResponse(WebClientResponseException ex) {
        log.warn("Upstream service error: status={}, message={}", ex.getStatusCode(), ex.getMessage());
        return new ErrorResponse("UPSTREAM_ERROR", "Upstream service returned error: " + ex.getStatusCode());
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(WebClientRequestException.class)
    public ErrorResponse handleWebClientRequest(WebClientRequestException ex) {
        log.error("Upstream connection error: {}", ex.getMessage());
        return new ErrorResponse("UPSTREAM_CONNECTION_ERROR", "Unable to connect to upstream service");
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(UpstreamServiceException.class)
    public ErrorResponse handleUpstreamService(UpstreamServiceException ex) {
        log.error("Upstream 5xx error: {}", ex.getMessage());
        return new ErrorResponse("UPSTREAM_ERROR", ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
    }
}
