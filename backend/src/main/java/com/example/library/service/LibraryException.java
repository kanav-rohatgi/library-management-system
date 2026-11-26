package com.example.library.service;

import org.springframework.http.HttpStatus;

public class LibraryException extends RuntimeException {
    private final HttpStatus status;

    private LibraryException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static LibraryException missing(String message) {
        return new LibraryException(HttpStatus.NOT_FOUND, message);
    }

    public static LibraryException conflict(String message) {
        return new LibraryException(HttpStatus.CONFLICT, message);
    }

    public static LibraryException invalid(String message) {
        return new LibraryException(HttpStatus.BAD_REQUEST, message);
    }

    public static LibraryException unauthorized() {
        return new LibraryException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
