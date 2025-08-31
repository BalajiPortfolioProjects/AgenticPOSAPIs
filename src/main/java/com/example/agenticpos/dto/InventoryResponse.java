package com.example.agenticpos.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long locationId;
    private String locationName;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer lowStockThreshold;
    private Boolean isLowStock;
    private LocalDateTime updatedAt;
    
    public static InventoryResponse from(com.example.agenticpos.entity.Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .productSku(inventory.getProduct().getSku())
                .locationId(inventory.getLocation().getId())
                .locationName(inventory.getLocation().getName())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .isLowStock(inventory.isLowStock())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
