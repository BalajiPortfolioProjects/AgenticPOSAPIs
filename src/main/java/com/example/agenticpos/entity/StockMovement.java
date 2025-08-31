package com.example.agenticpos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @NotNull(message = "Movement type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType movementType;
    
    @NotNull(message = "Quantity is required")
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "previous_quantity")
    private Integer previousQuantity;
    
    @Column(name = "new_quantity")
    private Integer newQuantity;
    
    @Size(max = 500, message = "Reference must not exceed 500 characters")
    private String reference;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id")
    private Location fromLocation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id")
    private Location toLocation;
    
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum MovementType {
        ADJUSTMENT_IN("Stock Adjustment - Increase"),
        ADJUSTMENT_OUT("Stock Adjustment - Decrease"),
        TRANSFER_IN("Transfer In"),
        TRANSFER_OUT("Transfer Out"),
        SALE("Sale"),
        RETURN("Return"),
        DAMAGE("Damage/Loss"),
        INITIAL_STOCK("Initial Stock");
        
        private final String description;
        
        MovementType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
