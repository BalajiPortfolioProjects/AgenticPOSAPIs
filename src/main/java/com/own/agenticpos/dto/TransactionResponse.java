package com.own.agenticpos.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.own.agenticpos.entity.Transaction.TransactionType;
import com.own.agenticpos.entity.Transaction.TransactionStatus;
import com.own.agenticpos.entity.Transaction.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionNumber;
    private TransactionType type;
    private String typeDescription;
    private TransactionStatus status;
    private String statusDescription;
    private Long locationId;
    private String locationName;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal subtotal;
    private BigDecimal finalAmount;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private PaymentMethod paymentMethod;
    private String paymentMethodDescription;
    private String paymentReference;
    private String notes;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TransactionItemResponse> items;
    
    public static TransactionResponse from(com.own.agenticpos.entity.Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionNumber(transaction.getTransactionNumber())
                .type(transaction.getType())
                .typeDescription(transaction.getType().getDescription())
                .status(transaction.getStatus())
                .statusDescription(transaction.getStatus().getDescription())
                .locationId(transaction.getLocation().getId())
                .locationName(transaction.getLocation().getName())
                .totalAmount(transaction.getTotalAmount())
                .taxAmount(transaction.getTaxAmount())
                .discountAmount(transaction.getDiscountAmount())
                .subtotal(transaction.getSubtotal())
                .finalAmount(transaction.getFinalAmount())
                .customerName(transaction.getCustomerName())
                .customerEmail(transaction.getCustomerEmail())
                .customerPhone(transaction.getCustomerPhone())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentMethodDescription(transaction.getPaymentMethod().getDescription())
                .paymentReference(transaction.getPaymentReference())
                .notes(transaction.getNotes())
                .createdBy(transaction.getCreatedBy())
                .updatedBy(transaction.getUpdatedBy())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .items(transaction.getItems() != null ? 
                    transaction.getItems().stream()
                        .map(TransactionItemResponse::from)
                        .toList() : null)
                .build();
    }
}
