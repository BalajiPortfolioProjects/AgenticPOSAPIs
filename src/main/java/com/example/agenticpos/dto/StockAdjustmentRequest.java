package com.example.agenticpos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.example.agenticpos.entity.StockMovement.MovementType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Location ID is required")
    private Long locationId;
    
    @NotNull(message = "Adjustment quantity is required")
    private Integer adjustmentQuantity; // Positive for increase, negative for decrease
    
    @NotNull(message = "Movement type is required")
    private MovementType movementType;
    
    @Size(max = 500, message = "Reference must not exceed 500 characters")
    private String reference;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
}
