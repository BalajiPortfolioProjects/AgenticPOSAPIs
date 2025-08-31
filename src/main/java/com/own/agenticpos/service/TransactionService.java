package com.own.agenticpos.service;

import com.own.agenticpos.dto.*;
import com.own.agenticpos.entity.*;
import com.own.agenticpos.exception.InsufficientStockException;
import com.own.agenticpos.exception.InvalidReturnException;
import com.own.agenticpos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {
    
    private static final String LOCATION_NOT_FOUND_MSG = "Location not found";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found";
    private static final String INVENTORY_NOT_FOUND_MSG = "Inventory not found";
    
    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final Random random = new Random();
    
    /**
     * Get all transactions with pagination
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Transaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return transactions.map(TransactionResponse::from);
    }
    
    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public Optional<TransactionResponse> getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(TransactionResponse::from);
    }
    
    /**
     * Process sale transaction
     */
    public TransactionResponse processSaleTransaction(SaleTransactionRequest request) {
        Location location = locationRepository.findByIdAndActiveTrue(request.getLocationId())
                .orElseThrow(() -> new RuntimeException(LOCATION_NOT_FOUND_MSG));
        
        // Validate inventory availability
        validateInventoryForSale(request.getItems(), location.getId());
        
        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionNumber(generateTransactionNumber("SALE"))
                .type(Transaction.TransactionType.SALE)
                .status(Transaction.TransactionStatus.PENDING)
                .location(location)
                .taxAmount(request.getTaxAmount())
                .discountAmount(request.getDiscountAmount())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .paymentMethod(request.getPaymentMethod())
                .paymentReference(request.getPaymentReference())
                .notes(request.getNotes())
                .createdBy(request.getCreatedBy())
                .build();
        
        // Save transaction first to get ID
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Calculate totals and create items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<TransactionItem> items = new ArrayList<>();
        for (TransactionItemRequest itemRequest : request.getItems()) {
            TransactionItem item = createTransactionItem(savedTransaction, itemRequest);
            items.add(item);
            subtotal = subtotal.add(item.getLineTotal());
        }
        
        savedTransaction.setSubtotal(subtotal);
        savedTransaction.setTotalAmount(savedTransaction.getFinalAmount());
        savedTransaction.setItems(items);
        
        // Update transaction with calculated totals
        savedTransaction = transactionRepository.save(savedTransaction);
        
        // Update inventory and create stock movements
        updateInventoryForSale(items, location);
        
        // Complete transaction
        savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        savedTransaction = transactionRepository.save(savedTransaction);
        
        return TransactionResponse.from(savedTransaction);
    }
    
    /**
     * Process purchase transaction
     */
    public TransactionResponse processPurchaseTransaction(PurchaseTransactionRequest request) {
        Location location = locationRepository.findByIdAndActiveTrue(request.getLocationId())
                .orElseThrow(() -> new RuntimeException(LOCATION_NOT_FOUND_MSG));
        
        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionNumber(generateTransactionNumber("PURCHASE"))
                .type(Transaction.TransactionType.PURCHASE)
                .status(Transaction.TransactionStatus.PENDING)
                .location(location)
                .taxAmount(request.getTaxAmount())
                .discountAmount(request.getDiscountAmount())
                .customerName(request.getSupplierName()) // Using customer fields for supplier info
                .customerEmail(request.getSupplierEmail())
                .customerPhone(request.getSupplierPhone())
                .paymentMethod(request.getPaymentMethod())
                .paymentReference(request.getPaymentReference())
                .notes(request.getNotes())
                .createdBy(request.getCreatedBy())
                .build();
        
        // Save transaction first to get ID
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Calculate totals and create items
        BigDecimal subtotal = BigDecimal.ZERO;
        List<TransactionItem> items = new ArrayList<>();
        for (TransactionItemRequest itemRequest : request.getItems()) {
            TransactionItem item = createTransactionItem(savedTransaction, itemRequest);
            items.add(item);
            subtotal = subtotal.add(item.getLineTotal());
        }
        
        savedTransaction.setSubtotal(subtotal);
        savedTransaction.setTotalAmount(savedTransaction.getFinalAmount());
        savedTransaction.setItems(items);
        
        // Update transaction with calculated totals
        savedTransaction = transactionRepository.save(savedTransaction);
        
        // Update inventory and create stock movements
        updateInventoryForPurchase(items, location);
        
        // Complete transaction
        savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        savedTransaction = transactionRepository.save(savedTransaction);
        
        return TransactionResponse.from(savedTransaction);
    }
    
    /**
     * Process return transaction
     */
    public TransactionResponse processReturnTransaction(ReturnTransactionRequest request) {
        Transaction originalTransaction = transactionRepository.findById(request.getOriginalTransactionId())
                .orElseThrow(() -> new RuntimeException("Original transaction not found"));
        
        Location location = locationRepository.findByIdAndActiveTrue(request.getLocationId())
                .orElseThrow(() -> new RuntimeException(LOCATION_NOT_FOUND_MSG));
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG));
        
        // Find the original transaction item
        TransactionItem originalItem = originalTransaction.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in original transaction"));
        
        // Validate return quantity
        if (request.getReturnQuantity() > originalItem.getQuantity()) {
            throw new InvalidReturnException("Return quantity cannot exceed original quantity");
        }
        
        // Create return transaction
        Transaction returnTransaction = Transaction.builder()
                .transactionNumber(generateTransactionNumber("RETURN"))
                .type(Transaction.TransactionType.RETURN)
                .status(Transaction.TransactionStatus.PENDING)
                .location(location)
                .totalAmount(request.getRefundAmount())
                .subtotal(request.getRefundAmount())
                .paymentMethod(request.getRefundMethod())
                .paymentReference(request.getRefundReference())
                .notes(String.format("Return for transaction %s. Reason: %s. %s", 
                        originalTransaction.getTransactionNumber(), 
                        request.getReturnReason().getDescription(),
                        request.getNotes() != null ? request.getNotes() : ""))
                .createdBy(request.getCreatedBy())
                .build();
        
        // Create return item
        TransactionItem returnItem = TransactionItem.builder()
                .transaction(returnTransaction)
                .product(product)
                .quantity(request.getReturnQuantity())
                .unitPrice(originalItem.getUnitPrice())
                .lineTotal(request.getRefundAmount())
                .notes("Return item - Reason: " + request.getReturnReason().getDescription())
                .build();
        
        returnTransaction.setItems(List.of(returnItem));
        
        // Save transaction
        returnTransaction = transactionRepository.save(returnTransaction);
        
        // Update inventory (add back to stock)
        updateInventoryForReturn(returnItem, location);
        
        // Complete transaction
        returnTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        returnTransaction = transactionRepository.save(returnTransaction);
        
        return TransactionResponse.from(returnTransaction);
    }
    
    // Helper methods
    private void validateInventoryForSale(List<TransactionItemRequest> items, Long locationId) {
        for (TransactionItemRequest itemRequest : items) {
            Inventory inventory = inventoryRepository.findByProductIdAndLocationId(
                    itemRequest.getProductId(), locationId)
                    .orElseThrow(() -> new RuntimeException("Product not available at this location"));
            
            if (inventory.getAvailableQuantity() < itemRequest.getQuantity()) {
                Product product = inventory.getProduct();
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product %s. Available: %d, Required: %d",
                                product.getName(), inventory.getAvailableQuantity(), itemRequest.getQuantity()));
            }
        }
    }
    
    private TransactionItem createTransactionItem(Transaction transaction, TransactionItemRequest itemRequest) {
        Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG));
        
        BigDecimal unitPrice = itemRequest.getUnitPriceOverride() != null ? 
                itemRequest.getUnitPriceOverride() : product.getPrice();
        
        return TransactionItem.builder()
                .transaction(transaction)
                .product(product)
                .quantity(itemRequest.getQuantity())
                .unitPrice(unitPrice)
                .discount(itemRequest.getDiscount())
                .taxAmount(itemRequest.getTaxAmount())
                .notes(itemRequest.getNotes())
                .build();
    }
    
    private void updateInventoryForSale(List<TransactionItem> items, Location location) {
        for (TransactionItem item : items) {
            Inventory inventory = inventoryRepository.findByProductIdAndLocationId(
                    item.getProduct().getId(), location.getId())
                    .orElseThrow(() -> new RuntimeException(INVENTORY_NOT_FOUND_MSG));
            
            Integer previousQuantity = inventory.getQuantity();
            Integer newQuantity = previousQuantity - item.getQuantity();
            inventory.setQuantity(newQuantity);
            inventoryRepository.save(inventory);
            
            // Create stock movement
            StockMovementData stockData = StockMovementData.forSale(item.getProduct(), location, 
                    item.getQuantity(), previousQuantity, newQuantity,
                    item.getTransaction().getTransactionNumber(), item.getTransaction().getCreatedBy());
            createStockMovement(stockData);
        }
    }
    
    private void updateInventoryForPurchase(List<TransactionItem> items, Location location) {
        for (TransactionItem item : items) {
            Inventory inventory = inventoryRepository.findByProductIdAndLocationId(
                    item.getProduct().getId(), location.getId())
                    .orElse(Inventory.builder()
                            .product(item.getProduct())
                            .location(location)
                            .quantity(0)
                            .reservedQuantity(0)
                            .lowStockThreshold(item.getProduct().getLowStockThreshold())
                            .build());
            
            Integer previousQuantity = inventory.getQuantity();
            Integer newQuantity = previousQuantity + item.getQuantity();
            inventory.setQuantity(newQuantity);
            inventoryRepository.save(inventory);
            
            // Create stock movement
            StockMovementData stockData = StockMovementData.forPurchase(item.getProduct(), location, 
                    item.getQuantity(), previousQuantity, newQuantity,
                    item.getTransaction().getTransactionNumber(), item.getTransaction().getCreatedBy());
            createStockMovement(stockData);
        }
    }
    
    private void updateInventoryForReturn(TransactionItem returnItem, Location location) {
        Inventory inventory = inventoryRepository.findByProductIdAndLocationId(
                returnItem.getProduct().getId(), location.getId())
                .orElseThrow(() -> new RuntimeException(INVENTORY_NOT_FOUND_MSG));
        
        Integer previousQuantity = inventory.getQuantity();
        Integer newQuantity = previousQuantity + returnItem.getQuantity();
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
        
        // Create stock movement
        StockMovementData stockData = StockMovementData.forReturn(returnItem.getProduct(), location, 
                returnItem.getQuantity(), previousQuantity, newQuantity,
                returnItem.getTransaction().getTransactionNumber(), returnItem.getTransaction().getCreatedBy());
        createStockMovement(stockData);
    }
    
    // Helper class for stock movement data
    private static class StockMovementData {
        final Product product;
        final Location location;
        final StockMovement.MovementType movementType;
        final Integer quantity;
        final Integer previousQuantity;
        final Integer newQuantity;
        final String reference;
        final String notes;
        final String createdBy;
        
        private StockMovementData(Builder builder) {
            this.product = builder.product;
            this.location = builder.location;
            this.movementType = builder.movementType;
            this.quantity = builder.quantity;
            this.previousQuantity = builder.previousQuantity;
            this.newQuantity = builder.newQuantity;
            this.reference = builder.reference;
            this.notes = builder.notes;
            this.createdBy = builder.createdBy;
        }
        
        static StockMovementData forSale(Product product, Location location, Integer quantity,
                                       Integer previousQuantity, Integer newQuantity, String transactionNumber, String createdBy) {
            return new Builder()
                    .product(product)
                    .location(location)
                    .movementType(StockMovement.MovementType.SALE)
                    .quantity(quantity)
                    .previousQuantity(previousQuantity)
                    .newQuantity(newQuantity)
                    .reference("Sale - Transaction " + transactionNumber)
                    .notes("Product sold")
                    .createdBy(createdBy)
                    .build();
        }
        
        static StockMovementData forPurchase(Product product, Location location, Integer quantity,
                                           Integer previousQuantity, Integer newQuantity, String transactionNumber, String createdBy) {
            return new Builder()
                    .product(product)
                    .location(location)
                    .movementType(StockMovement.MovementType.ADJUSTMENT_IN)
                    .quantity(quantity)
                    .previousQuantity(previousQuantity)
                    .newQuantity(newQuantity)
                    .reference("Purchase - Transaction " + transactionNumber)
                    .notes("Product purchased")
                    .createdBy(createdBy)
                    .build();
        }
        
        static StockMovementData forReturn(Product product, Location location, Integer quantity,
                                         Integer previousQuantity, Integer newQuantity, String transactionNumber, String createdBy) {
            return new Builder()
                    .product(product)
                    .location(location)
                    .movementType(StockMovement.MovementType.RETURN)
                    .quantity(quantity)
                    .previousQuantity(previousQuantity)
                    .newQuantity(newQuantity)
                    .reference("Return - Transaction " + transactionNumber)
                    .notes("Product returned")
                    .createdBy(createdBy)
                    .build();
        }
        
        private static class Builder {
            private Product product;
            private Location location;
            private StockMovement.MovementType movementType;
            private Integer quantity;
            private Integer previousQuantity;
            private Integer newQuantity;
            private String reference;
            private String notes;
            private String createdBy;
            
            Builder product(Product product) { this.product = product; return this; }
            Builder location(Location location) { this.location = location; return this; }
            Builder movementType(StockMovement.MovementType movementType) { this.movementType = movementType; return this; }
            Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
            Builder previousQuantity(Integer previousQuantity) { this.previousQuantity = previousQuantity; return this; }
            Builder newQuantity(Integer newQuantity) { this.newQuantity = newQuantity; return this; }
            Builder reference(String reference) { this.reference = reference; return this; }
            Builder notes(String notes) { this.notes = notes; return this; }
            Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
            
            StockMovementData build() {
                return new StockMovementData(this);
            }
        }
    }
    
    private void createStockMovement(StockMovementData data) {
        StockMovement movement = StockMovement.builder()
                .product(data.product)
                .location(data.location)
                .movementType(data.movementType)
                .quantity(data.quantity)
                .previousQuantity(data.previousQuantity)
                .newQuantity(data.newQuantity)
                .reference(data.reference)
                .notes(data.notes)
                .createdBy(data.createdBy)
                .build();
        
        stockMovementRepository.save(movement);
    }
    
    private String generateTransactionNumber(String prefix) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("%s-%s-%03d", prefix, timestamp, 
                random.nextInt(1000));
    }
}
