package com.hostelmanagement.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Converts exceptions into JSON: { "message": "..." } so the frontend can show them. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Operation failed: duplicate value or the record is linked to other data."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Invalid data sent to the server. Please check the form values."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOther(Exception ex) {
        // Spring MVC's own exceptions (404 not found, 405 wrong method, 415 ...) keep their status code
        if (ex instanceof ErrorResponse er) {
            return ResponseEntity.status(er.getStatusCode())
                    .body(Map.of("message", "Request failed (" + er.getStatusCode().value() + ")"));
        }
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Server error: " + ex.getMessage()));
    }
}
