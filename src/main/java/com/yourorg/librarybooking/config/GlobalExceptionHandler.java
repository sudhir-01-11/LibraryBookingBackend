package com.yourorg.librarybooking.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.core.NestedExceptionUtils;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(ex);
        String message = rootCause != null ? rootCause.getMessage() : ex.getMessage();
        
        if (message != null && message.contains("ex_bookings_active_user_time_range")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "You already have an active booking during this time period. You cannot book two overlapping sessions."));
        }
        if (message != null && message.contains("ex_bookings_active_seat_time_range")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "This seat was just taken by someone else! Please pick another seat."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Database constraint violation occurred."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(ex);
        String message = rootCause != null ? rootCause.getMessage() : ex.getMessage();
        
        if (message != null && message.contains("ex_bookings_active_user_time_range")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "You already have an active booking during this time period. You cannot book two overlapping sessions."));
        }
        
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An unexpected error occurred: " + message));
    }
}
