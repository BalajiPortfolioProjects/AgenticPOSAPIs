package com.own.agenticpos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.own.agenticpos.entity.Transaction.PaymentMethod;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnTransactionRequest {
    
    @NotNull(message = "Original transaction ID is required")
    private Long originalTransactionId;
    
    @NotNull(message = "Location ID is required")
    private Long locationId;
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Return quantity is required")
    @Min(value = 1, message = "Return quantity must be at least 1")
    private Integer returnQuantity;
    
    @NotNull(message = "Return reason is required")
    private ReturnReason returnReason;
    
    @DecimalMin(value = "0.0", message = "Refund amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Refund amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal refundAmount;
    
    @NotNull(message = "Refund method is required")
    private PaymentMethod refundMethod;
    
    @Size(max = 100, message = "Refund reference must not exceed 100 characters")
    private String refundReference;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
    
    public enum ReturnReason {
        DEFECTIVE("Defective Product"),
        WRONG_ITEM("Wrong Item"),
        NOT_SATISFIED("Customer Not Satisfied"),
        DAMAGED_IN_TRANSIT("Damaged in Transit"),
        DUPLICATE_ORDER("Duplicate Order"),
        OTHER("Other");
        
        private final String description;
        
        ReturnReason(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
