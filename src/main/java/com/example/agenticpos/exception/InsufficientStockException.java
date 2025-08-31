package com.example.agenticpos.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(Integer currentQuantity, Integer requestedQuantity) {
        super(String.format("Insufficient stock. Current quantity: %d, Requested: %d", 
                currentQuantity, requestedQuantity));
    }
}
