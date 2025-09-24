package com.example.song.exception;

import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<?> handleDuplicate(DuplicateException ex) {
        // важливо: у відповіді має бути і id, і errorMessage/errorCode
        String msg = ex.getMessage();
        // витягнемо id з повідомлення, якщо треба
        Long id = null;
        try {
            String[] parts = msg.split(" ");
            id = Long.parseLong(parts[parts.length - 1]);
        } catch (Exception ignored) {}

        Map<String,Object> body = new HashMap<>();
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


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        return ResponseEntity.status(500).body(Map.of(
                "errorMessage", "Internal error",
                "errorCode", "500"
        ));
    }


}