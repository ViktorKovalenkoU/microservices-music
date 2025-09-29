package com.example.song.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationErrorsException.class)
    public ResponseEntity<?> handleValidationErrors(ValidationErrorsException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "errorMessage", "Validation error",
                "details", ex.getErrors(),
                "errorCode", "400"
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.badRequest().body(Map.of(
                "errorMessage", "Validation error",
                "errorCode", "400",
                "details", errors
        ));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<?> handleDuplicate(DuplicateException ex) {
        String msg = ex.getMessage();
        Long id = null;
        try {
            String[] parts = msg.split(" ");
            id = Long.parseLong(parts[parts.length - 1]);
        } catch (Exception ignored) {}

        Map<String, Object> body = new HashMap<>();
        body.put("errorMessage", msg);
        body.put("errorCode", "409");
        if (id != null) {
            body.put("id", id);
        }

        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of(
                "errorMessage", ex.getMessage(),
                "errorCode", "404"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "errorMessage", ex.getMessage(),
                "errorCode", "400"
        ));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<?> handleNumberFormat(NumberFormatException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "errorMessage", "Invalid id",
                "errorCode", "400"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        return ResponseEntity.status(500).body(Map.of(
                "errorMessage", "Internal error",
                "errorCode", "500"
        ));
    }
}