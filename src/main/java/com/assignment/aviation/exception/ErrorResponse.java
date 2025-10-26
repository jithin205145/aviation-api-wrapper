package com.assignment.aviation.exception;

/**
 * Consistent error response contract for all API errors.
 *
 * @param code Error code (e.g., NOT_FOUND, UPSTREAM_ERROR)
 * @param message Human-readable error message
 */
public record ErrorResponse(String code, String message) {}

