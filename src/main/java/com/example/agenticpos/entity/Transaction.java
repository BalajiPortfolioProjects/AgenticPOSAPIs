package com.example.agenticpos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Transaction number is required")
    @Size(max = 50, message = "Transaction number must not exceed 50 characters")
    @Column(name = "transaction_number", nullable = false, unique = true)
    private String transactionNumber;
    
    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @NotNull(message = "Transaction status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Total amount must have at most 10 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    
    @DecimalMin(value = "0.0", message = "Tax amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Tax amount must have at most 10 integer digits and 2 decimal places")
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Discount amount must have at most 10 integer digits and 2 decimal places")
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Subtotal cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Subtotal must have at most 10 integer digits and 2 decimal places")
    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;
    
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    @Column(name = "customer_name")
    private String customerName;
    
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    @Email(message = "Customer email must be valid")
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Size(max = 20, message = "Customer phone must not exceed 20 characters")
    @Column(name = "customer_phone")
    private String customerPhone;
    
    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    @Column(name = "payment_reference")
    private String paymentReference;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    @Column(name = "created_by")
    private String createdBy;
    
    @Size(max = 100, message = "Updated by must not exceed 100 characters")
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionItem> items;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper method to calculate final amount
    public BigDecimal getFinalAmount() {
        return subtotal.add(taxAmount).subtract(discountAmount);
    }
    
    public enum TransactionType {
        SALE("Sale"),
        PURCHASE("Purchase"),
        RETURN("Return"),
        ADJUSTMENT("Adjustment");
        
        private final String description;
        
        TransactionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum TransactionStatus {
        PENDING("Pending"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");
        
        private final String description;
        
        TransactionStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum PaymentMethod {
        CASH("Cash"),
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        BANK_TRANSFER("Bank Transfer"),
        DIGITAL_WALLET("Digital Wallet"),
        CHECK("Check"),
        STORE_CREDIT("Store Credit");
        
        private final String description;
        
        PaymentMethod(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
