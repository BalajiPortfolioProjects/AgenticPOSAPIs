package com.example.agenticpos.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.example.agenticpos.entity.StockMovement.MovementType;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long locationId;
    private String locationName;
    private MovementType movementType;
    private String movementDescription;
    private Integer quantity;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String reference;
    private String notes;
    private Long fromLocationId;
    private String fromLocationName;
    private Long toLocationId;
    private String toLocationName;
    private String createdBy;
    private LocalDateTime createdAt;
    
    public static StockMovementResponse from(com.example.agenticpos.entity.StockMovement movement) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .productName(movement.getProduct().getName())
                .productSku(movement.getProduct().getSku())
                .locationId(movement.getLocation().getId())
                .locationName(movement.getLocation().getName())
                .movementType(movement.getMovementType())
                .movementDescription(movement.getMovementType().getDescription())
                .quantity(movement.getQuantity())
                .previousQuantity(movement.getPreviousQuantity())
                .newQuantity(movement.getNewQuantity())
                .reference(movement.getReference())
                .notes(movement.getNotes())
                .fromLocationId(movement.getFromLocation() != null ? movement.getFromLocation().getId() : null)
                .fromLocationName(movement.getFromLocation() != null ? movement.getFromLocation().getName() : null)
                .toLocationId(movement.getToLocation() != null ? movement.getToLocation().getId() : null)
                .toLocationName(movement.getToLocation() != null ? movement.getToLocation().getName() : null)
                .createdBy(movement.getCreatedBy())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
