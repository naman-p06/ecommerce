package com.ecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — resource does not exist
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    // 400 — client sent bad/invalid data
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    // 401 — authentication failed (wrong password, invalid token)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {

        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
    }

    // 403 — authenticated but not allowed (e.g. USER trying ADMIN endpoint)
    // Spring Security throws AccessDeniedException — without this handler it returns
    // an HTML error page instead of clean JSON
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        return build(HttpStatus.FORBIDDEN, "Forbidden",
                "You do not have permission to access this resource", request);
    }

    // 400 — @Valid annotation failures (blank name, negative price, etc.)
    // Collects ALL field errors into one readable message instead of returning only the first
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, "Validation Failed", message, request);
    }

    // 409 — duplicate email hit the DB unique constraint
    // This is a safety net: even if UserService checks first, a race condition could
    // let two registrations through simultaneously; the DB constraint catches it
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseConflict(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        return build(HttpStatus.CONFLICT, "Conflict",
                "A record with the provided data already exists", request);
    }

    // 500 — anything unexpected that wasn't caught above
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {

        // Log the real error internally (Day 10 adds @Slf4j logging everywhere)
        // Don't expose internal details to the client
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.", request);
    }

    // Private helper to keep all handlers clean and consistent
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, String message, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                status.value(), error, message, request.getRequestURI()
        );
        return new ResponseEntity<>(body, status);
    }
}