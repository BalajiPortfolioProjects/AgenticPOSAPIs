package com.own.agenticpos.controller;

import com.own.agenticpos.dto.*;
import com.own.agenticpos.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@Validated
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    /**
     * Get inventory overview with summary statistics
     * GET /api/v1/inventory
     */
    @GetMapping
    public ResponseEntity<InventoryOverviewResponse> getInventoryOverview() {
        InventoryOverviewResponse overview = inventoryService.getInventoryOverview();
        return ResponseEntity.ok(overview);
    }
    
    /**
     * Get inventory for a specific product across all locations
     * GET /api/v1/inventory/{productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<List<InventoryResponse>> getInventoryByProduct(
            @PathVariable @Min(1) Long productId) {
        List<InventoryResponse> inventories = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(inventories);
    }
    
    /**
     * Adjust stock levels for a product at a location
     * POST /api/v1/inventory/adjust
     */
    @PostMapping("/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        InventoryResponse adjustedInventory = inventoryService.adjustStock(request);
        return ResponseEntity.ok(adjustedInventory);
    }
    
    /**
     * Transfer stock between locations
     * POST /api/v1/inventory/transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<List<InventoryResponse>> transferStock(@Valid @RequestBody StockTransferRequest request) {
        List<InventoryResponse> transferResults = inventoryService.transferStock(request);
        return ResponseEntity.ok(transferResults);
    }
    
    /**
     * Get stock movements history with pagination
     * GET /api/v1/inventory/movements
     */
    @GetMapping("/movements")
    public ResponseEntity<PagedResponse<StockMovementResponse>> getStockMovements(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<StockMovementResponse> movements = inventoryService.getStockMovements(page, size, sortBy, sortDir);
        PagedResponse<StockMovementResponse> response = PagedResponse.from(movements);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get stock movements for a specific product
     * GET /api/v1/inventory/movements/product/{productId}
     */
    @GetMapping("/movements/product/{productId}")
    public ResponseEntity<List<StockMovementResponse>> getStockMovementsByProduct(
            @PathVariable @Min(1) Long productId) {
        List<StockMovementResponse> movements = inventoryService.getStockMovementsByProduct(productId);
        return ResponseEntity.ok(movements);
    }
}
