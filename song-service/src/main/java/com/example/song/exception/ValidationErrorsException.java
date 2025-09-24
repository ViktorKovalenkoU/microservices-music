package com.example.song.exception;

import java.util.Map;

public class ValidationErrorsException extends RuntimeException {
    private final Map<String, String> errors;

    public ValidationErrorsException(Map<String, String> errors) {
        super("Validation error");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}