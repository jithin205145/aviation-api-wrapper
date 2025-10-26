package com.assignment.aviation.exception;

/**
 * Exception thrown when upstream aviation API service is unavailable or returns errors.
 */
public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String message) {
        super(message);
    }

    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

