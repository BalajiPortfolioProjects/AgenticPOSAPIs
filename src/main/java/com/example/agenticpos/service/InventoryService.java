package com.example.agenticpos.service;

import com.example.agenticpos.dto.*;
import com.example.agenticpos.entity.*;
import com.example.agenticpos.exception.InsufficientStockException;
import com.example.agenticpos.exception.InvalidTransferException;
import com.example.agenticpos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final StockMovementRepository stockMovementRepository;
    
    /**
     * Get inventory overview with summary statistics
     */
    @Transactional(readOnly = true)
    public InventoryOverviewResponse getInventoryOverview() {
        Long totalProducts = inventoryRepository.countDistinctProducts();
        Long totalLocations = inventoryRepository.countDistinctLocations();
        Integer totalStockUnits = inventoryRepository.getTotalStockUnits();
        BigDecimal totalInventoryValue = inventoryRepository.getTotalInventoryValue();
        
        List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();
        
        // Get location summaries
        List<Location> locations = locationRepository.findAllActiveOrderByName();
        List<InventoryOverviewResponse.LocationInventorySummary> locationSummaries = locations.stream()
                .map(this::createLocationSummary)
                .collect(Collectors.toList());
        
        // Convert low stock items
        List<InventoryOverviewResponse.LowStockItem> lowStockItemsList = lowStockItems.stream()
                .map(this::createLowStockItem)
                .collect(Collectors.toList());
        
        return InventoryOverviewResponse.builder()
                .totalProducts(totalProducts)
                .totalLocations(totalLocations)
                .totalStockUnits(totalStockUnits)
                .lowStockItems(lowStockItems.size())
                .totalInventoryValue(totalInventoryValue)
                .locationSummaries(locationSummaries)
                .lowStockItemsList(lowStockItemsList)
                .build();
    }
    
    /**
     * Get inventory for a specific product across all locations
     */
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByProduct(Long productId) {
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        return inventories.stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all stock movements with pagination
     */
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getStockMovements(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<StockMovement> movements = stockMovementRepository.findAllByOrderByCreatedAtDesc(pageable);
        return movements.map(StockMovementResponse::from);
    }
    
    /**
     * Get stock movements for a specific product
     */
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementsByProduct(Long productId) {
        List<StockMovement> movements = stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
        return movements.stream()
                .map(StockMovementResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Adjust stock levels for a product at a location
     */
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Location location = locationRepository.findByIdAndActiveTrue(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        // Get or create inventory record
        Inventory inventory = inventoryRepository.findByProductIdAndLocationId(
                request.getProductId(), request.getLocationId())
                .orElse(Inventory.builder()
                        .product(product)
                        .location(location)
                        .quantity(0)
                        .reservedQuantity(0)
                        .lowStockThreshold(product.getLowStockThreshold())
                        .build());
        
        Integer previousQuantity = inventory.getQuantity();
        Integer newQuantity = previousQuantity + request.getAdjustmentQuantity();
        
        // Validate new quantity is not negative
        if (newQuantity < 0) {
            throw new InsufficientStockException("Insufficient stock. Current quantity: " + previousQuantity + 
                    ", Adjustment: " + request.getAdjustmentQuantity());
        }
        
        inventory.setQuantity(newQuantity);
        inventory = inventoryRepository.save(inventory);
        
        // Create stock movement record
        StockMovementBuilder.builder()
                .product(product)
                .location(location)
                .movementType(request.getMovementType())
                .quantity(Math.abs(request.getAdjustmentQuantity()))
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .reference(request.getReference())
                .notes(request.getNotes())
                .createdBy(request.getCreatedBy())
                .buildAndSave(stockMovementRepository);
        
        return InventoryResponse.from(inventory);
    }
    
    /**
     * Transfer stock between locations
     */
    public List<InventoryResponse> transferStock(StockTransferRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Location fromLocation = locationRepository.findByIdAndActiveTrue(request.getFromLocationId())
                .orElseThrow(() -> new RuntimeException("From location not found"));
        
        Location toLocation = locationRepository.findByIdAndActiveTrue(request.getToLocationId())
                .orElseThrow(() -> new RuntimeException("To location not found"));
        
        if (request.getFromLocationId().equals(request.getToLocationId())) {
            throw new InvalidTransferException("Cannot transfer to the same location");
        }
        
        // Get source inventory
        Inventory fromInventory = inventoryRepository.findByProductIdAndLocationId(
                request.getProductId(), request.getFromLocationId())
                .orElseThrow(() -> new RuntimeException("No inventory found at source location"));
        
        // Check available quantity
        if (fromInventory.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock at source location. Available: " + 
                    fromInventory.getAvailableQuantity() + ", Requested: " + request.getQuantity());
        }
        
        // Update source inventory
        Integer fromPreviousQuantity = fromInventory.getQuantity();
        Integer fromNewQuantity = fromPreviousQuantity - request.getQuantity();
        fromInventory.setQuantity(fromNewQuantity);
        fromInventory = inventoryRepository.save(fromInventory);
        
        // Get or create destination inventory
        Inventory toInventory = inventoryRepository.findByProductIdAndLocationId(
                request.getProductId(), request.getToLocationId())
                .orElse(Inventory.builder()
                        .product(product)
                        .location(toLocation)
                        .quantity(0)
                        .reservedQuantity(0)
                        .lowStockThreshold(product.getLowStockThreshold())
                        .build());
        
        Integer toPreviousQuantity = toInventory.getQuantity();
        Integer toNewQuantity = toPreviousQuantity + request.getQuantity();
        toInventory.setQuantity(toNewQuantity);
        toInventory = inventoryRepository.save(toInventory);
        
        // Create stock movement records
        StockMovementBuilder.builder()
                .product(product)
                .location(fromLocation)
                .movementType(StockMovement.MovementType.TRANSFER_OUT)
                .quantity(request.getQuantity())
                .previousQuantity(fromPreviousQuantity)
                .newQuantity(fromNewQuantity)
                .reference(request.getReference())
                .notes(request.getNotes())
                .createdBy(request.getCreatedBy())
                .fromLocation(fromLocation)
                .toLocation(toLocation)
                .buildAndSave(stockMovementRepository);
        
        StockMovementBuilder.builder()
                .product(product)
                .location(toLocation)
                .movementType(StockMovement.MovementType.TRANSFER_IN)
                .quantity(request.getQuantity())
                .previousQuantity(toPreviousQuantity)
                .newQuantity(toNewQuantity)
                .reference(request.getReference())
                .notes(request.getNotes())
                .createdBy(request.getCreatedBy())
                .fromLocation(fromLocation)
                .toLocation(toLocation)
                .buildAndSave(stockMovementRepository);
        
        return List.of(InventoryResponse.from(fromInventory), InventoryResponse.from(toInventory));
    }
    
    // Helper methods
    private InventoryOverviewResponse.LocationInventorySummary createLocationSummary(Location location) {
        List<Inventory> inventories = inventoryRepository.findByLocationId(location.getId());
        List<Inventory> lowStockItems = inventoryRepository.findLowStockItemsByLocation(location.getId());
        BigDecimal inventoryValue = inventoryRepository.getTotalInventoryValueByLocation(location.getId());
        
        return InventoryOverviewResponse.LocationInventorySummary.builder()
                .locationId(location.getId())
                .locationName(location.getName())
                .totalItems(inventories.size())
                .lowStockItems(lowStockItems.size())
                .inventoryValue(inventoryValue)
                .build();
    }
    
    private InventoryOverviewResponse.LowStockItem createLowStockItem(Inventory inventory) {
        Integer suggestedReorder = Math.max(inventory.getLowStockThreshold() * 2, 
                inventory.getLowStockThreshold() + 10);
        
        return InventoryOverviewResponse.LowStockItem.builder()
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .productSku(inventory.getProduct().getSku())
                .locationId(inventory.getLocation().getId())
                .locationName(inventory.getLocation().getName())
                .currentStock(inventory.getAvailableQuantity())
                .threshold(inventory.getLowStockThreshold())
                .suggestedReorder(suggestedReorder)
                .build();
    }
    
    // Helper class for building stock movements
    private static class StockMovementBuilder {
        private Product product;
        private Location location;
        private StockMovement.MovementType movementType;
        private Integer quantity;
        private Integer previousQuantity;
        private Integer newQuantity;
        private String reference;
        private String notes;
        private String createdBy;
        private Location fromLocation;
        private Location toLocation;
        
        public static StockMovementBuilder builder() {
            return new StockMovementBuilder();
        }
        
        public StockMovementBuilder product(Product product) {
            this.product = product;
            return this;
        }
        
        public StockMovementBuilder location(Location location) {
            this.location = location;
            return this;
        }
        
        public StockMovementBuilder movementType(StockMovement.MovementType movementType) {
            this.movementType = movementType;
            return this;
        }
        
        public StockMovementBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public StockMovementBuilder previousQuantity(Integer previousQuantity) {
            this.previousQuantity = previousQuantity;
            return this;
        }
        
        public StockMovementBuilder newQuantity(Integer newQuantity) {
            this.newQuantity = newQuantity;
            return this;
        }
        
        public StockMovementBuilder reference(String reference) {
            this.reference = reference;
            return this;
        }
        
        public StockMovementBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }
        
        public StockMovementBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }
        
        public StockMovementBuilder fromLocation(Location fromLocation) {
            this.fromLocation = fromLocation;
            return this;
        }
        
        public StockMovementBuilder toLocation(Location toLocation) {
            this.toLocation = toLocation;
            return this;
        }
        
        public void buildAndSave(StockMovementRepository repository) {
            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .location(location)
                    .movementType(movementType)
                    .quantity(quantity)
                    .previousQuantity(previousQuantity)
                    .newQuantity(newQuantity)
                    .reference(reference)
                    .notes(notes)
                    .createdBy(createdBy)
                    .fromLocation(fromLocation)
                    .toLocation(toLocation)
                    .build();
            
            repository.save(movement);
        }
    }
}
