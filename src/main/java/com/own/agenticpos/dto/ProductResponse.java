package com.own.agenticpos.dto;

import com.own.agenticpos.entity.Product;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private String category;
    private String sku;
    private Boolean active;
    private Boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor from Product entity
    public ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.lowStockThreshold = product.getLowStockThreshold();
        this.category = product.getCategory();
        this.sku = product.getSku();
        this.active = product.getActive();
        this.lowStock = product.isLowStock();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }
    
    // Static factory method
    public static ProductResponse from(Product product) {
        return new ProductResponse(product);
    }
}
