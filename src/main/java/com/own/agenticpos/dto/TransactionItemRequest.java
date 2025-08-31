package com.own.agenticpos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItemRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @DecimalMin(value = "0.0", message = "Unit price override cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Unit price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal unitPriceOverride;
    
    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Discount must have at most 10 integer digits and 2 decimal places")
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Tax amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Tax amount must have at most 10 integer digits and 2 decimal places")
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
