package com.example.agenticpos.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.example.agenticpos.entity.Transaction.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleTransactionRequest {
    
    @NotNull(message = "Location ID is required")
    private Long locationId;
    
    @NotEmpty(message = "Transaction items are required")
    @Valid
    private List<TransactionItemRequest> items;
    
    @DecimalMin(value = "0.0", message = "Tax amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Tax amount must have at most 10 integer digits and 2 decimal places")
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Discount amount must have at most 10 integer digits and 2 decimal places")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;
    
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    @Email(message = "Customer email must be valid")
    private String customerEmail;
    
    @Size(max = 20, message = "Customer phone must not exceed 20 characters")
    private String customerPhone;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    private String paymentReference;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
}
