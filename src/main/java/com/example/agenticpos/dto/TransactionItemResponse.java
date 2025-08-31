package com.example.agenticpos.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
    private String notes;
    
    public static TransactionItemResponse from(com.example.agenticpos.entity.TransactionItem item) {
        return TransactionItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discount(item.getDiscount())
                .taxAmount(item.getTaxAmount())
                .lineTotal(item.getLineTotal())
                .notes(item.getNotes())
                .build();
    }
}
