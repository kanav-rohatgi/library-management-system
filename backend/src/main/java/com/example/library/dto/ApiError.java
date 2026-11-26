package com.example.library.dto;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.Map;

public record ApiError(Instant timestamp, int status, String message, String path, Map<String, String> errors) {
    public static ApiError of(HttpServletRequest request, int status, String message, Map<String, String> errors) {
        return new ApiError(Instant.now(), status, message, request.getRequestURI(), errors);
    }
}
