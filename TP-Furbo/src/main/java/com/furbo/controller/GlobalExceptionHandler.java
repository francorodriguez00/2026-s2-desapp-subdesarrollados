package com.furbo.controller;

import com.furbo.exceptions.InvalidFilterException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
    }

    @ExceptionHandler(InvalidFilterException.class)
    public ResponseEntity<Map<String, String>> handle(InvalidFilterException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
