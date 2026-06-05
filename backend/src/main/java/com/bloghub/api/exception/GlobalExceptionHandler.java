package com.bloghub.api.exception;

import com.bloghub.api.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for all REST controllers.
 * Converts exceptions into consistent JSON error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** Handles 404 - resource not found */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse error = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /** Handles custom business logic exceptions */
    @ExceptionHandler(BlogApiException.class)
    public ResponseEntity<ErrorResponse> handleBlogApiException(
            BlogApiException ex, HttpServletRequest request) {
        log.warn("Blog API exception: {}", ex.getMessage());
        ErrorResponse error = buildError(ex.getStatus(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    /** Handles Spring Security authorization failures */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", request.getRequestURI());
        ErrorResponse error = buildError(HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /** Handles bad credentials (wrong password/username) */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.UNAUTHORIZED,
                "Invalid username or password", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /** Handles @Valid validation failures - returns field-level error map */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = err instanceof FieldError ? ((FieldError) err).getField() : err.getObjectName();
            validationErrors.put(field, err.getDefaultMessage());
        });

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed. Check 'validationErrors' for details.")
                .path(request.getRequestURI())
                .correlationId(correlationId())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /** Catch-all for unexpected exceptions */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse error = buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private ErrorResponse buildError(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .correlationId(correlationId())
                .build();
    }

    private String correlationId() {
        return MDC.get(CorrelationIdFilter.MDC_KEY);
    }
}
