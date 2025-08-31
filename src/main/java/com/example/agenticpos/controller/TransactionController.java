package com.example.agenticpos.controller;

import com.example.agenticpos.dto.*;
import com.example.agenticpos.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/transactions")
@Validated
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    /**
     * List all transactions (paginated)
     * GET /api/v1/transactions
     */
    @GetMapping
    public ResponseEntity<PagedResponse<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<TransactionResponse> transactions = transactionService.getAllTransactions(page, size, sortBy, sortDir);
        PagedResponse<TransactionResponse> response = PagedResponse.from(transactions);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get transaction details by ID
     * GET /api/v1/transactions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable @Min(1) Long id) {
        Optional<TransactionResponse> transaction = transactionService.getTransactionById(id);
        return transaction.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Process sale transaction
     * POST /api/v1/transactions/sale
     */
    @PostMapping("/sale")
    public ResponseEntity<TransactionResponse> processSaleTransaction(@Valid @RequestBody SaleTransactionRequest request) {
        TransactionResponse response = transactionService.processSaleTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Process purchase transaction
     * POST /api/v1/transactions/purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<TransactionResponse> processPurchaseTransaction(@Valid @RequestBody PurchaseTransactionRequest request) {
        TransactionResponse response = transactionService.processPurchaseTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Process return transaction
     * POST /api/v1/transactions/return
     */
    @PostMapping("/return")
    public ResponseEntity<TransactionResponse> processReturnTransaction(@Valid @RequestBody ReturnTransactionRequest request) {
        TransactionResponse response = transactionService.processReturnTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
