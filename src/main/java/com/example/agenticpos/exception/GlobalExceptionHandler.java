package com.example.agenticpos.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle validation errors for request body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed for one or more fields",
                LocalDateTime.now(),
                errors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle validation errors for request parameters
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed for request parameters",
                LocalDateTime.now(),
                errors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle illegal argument exceptions (e.g., duplicate SKU)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "BUSINESS_ERROR",
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle insufficient stock exceptions
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INSUFFICIENT_STOCK",
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle invalid transfer exceptions
     */
    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransferException(InvalidTransferException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_TRANSFER",
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle duplicate location exceptions
     */
    @ExceptionHandler(DuplicateLocationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLocationException(DuplicateLocationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "DUPLICATE_LOCATION",
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle invalid return exceptions
     */
    @ExceptionHandler(InvalidReturnException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReturnException(InvalidReturnException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_RETURN",
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                LocalDateTime.now(),
                null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Error response DTO
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ErrorResponse {
        private String code;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, String> details;
    }
}
