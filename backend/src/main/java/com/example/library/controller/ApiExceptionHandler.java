package com.example.library.controller;

import com.example.library.dto.ApiError;
import com.example.library.service.LibraryException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(LibraryException.class)
    ResponseEntity<ApiError> libraryError(LibraryException error, HttpServletRequest request) {
        return response(request, error.getStatus(), error.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> accessDenied(HttpServletRequest request) {
        return response(request, HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> constraintViolation(HttpServletRequest request) {
        return response(request, HttpStatus.CONFLICT, "The change conflicts with an existing record");
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    ResponseEntity<ApiError> databaseBusy(HttpServletRequest request) {
        return response(request, HttpStatus.CONFLICT, "A concurrent change interrupted this operation. Try again.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception error, HttpServletRequest request) {
        LOGGER.error("Request failed at {}", request.getRequestURI(), error);
        return response(request, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException error,
                                                                    HttpHeaders headers, HttpStatusCode status,
                                                                    WebRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        error.getBindingResult().getFieldErrors().forEach(field ->
                fields.putIfAbsent(field.getField(), field.getDefaultMessage()));
        return ResponseEntity.status(status).headers(headers)
                .body(ApiError.of(((ServletWebRequest) request).getRequest(), status.value(),
                        "Validation failed", fields));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception error, Object body, HttpHeaders headers,
                                                              HttpStatusCode status, WebRequest request) {
        HttpStatus known = HttpStatus.resolve(status.value());
        String message = known == null ? "Request failed" : known.getReasonPhrase();
        ApiError apiError = ApiError.of(((ServletWebRequest) request).getRequest(), status.value(), message, Map.of());
        return super.handleExceptionInternal(error, apiError, headers, status, request);
    }

    private ResponseEntity<ApiError> response(HttpServletRequest request, HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiError.of(request, status.value(), message, Map.of()));
    }
}
