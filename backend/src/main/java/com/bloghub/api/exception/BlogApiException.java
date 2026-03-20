package com.bloghub.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Generic API exception for business logic violations.
 * Carries an HTTP status and a descriptive message.
 */
public class BlogApiException extends RuntimeException {

    private final HttpStatus status;

    public BlogApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
