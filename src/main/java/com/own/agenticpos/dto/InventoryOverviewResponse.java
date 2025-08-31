package com.own.agenticpos.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOverviewResponse {
    private Long totalProducts;
    private Long totalLocations;
    private Integer totalStockUnits;
    private Integer lowStockItems;
    private BigDecimal totalInventoryValue;
    private List<LocationInventorySummary> locationSummaries;
    private List<LowStockItem> lowStockItemsList;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInventorySummary {
        private Long locationId;
        private String locationName;
        private Integer totalItems;
        private Integer lowStockItems;
        private BigDecimal inventoryValue;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockItem {
        private Long productId;
        private String productName;
        private String productSku;
        private Long locationId;
        private String locationName;
        private Integer currentStock;
        private Integer threshold;
        private Integer suggestedReorder;
    }
}
