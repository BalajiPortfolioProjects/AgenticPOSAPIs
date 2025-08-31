package com.example.agenticpos.repository;

import com.example.agenticpos.entity.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {
    
    List<TransactionItem> findByTransactionId(Long transactionId);
    
    List<TransactionItem> findByProductId(Long productId);
    
    @Query("SELECT ti FROM TransactionItem ti WHERE ti.product.id = :productId AND ti.transaction.createdAt BETWEEN :startDate AND :endDate")
    List<TransactionItem> findByProductAndDateRange(@Param("productId") Long productId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(ti.quantity), 0) FROM TransactionItem ti WHERE ti.product.id = :productId AND ti.transaction.type = 'SALE' AND ti.transaction.status = 'COMPLETED'")
    Integer getTotalQuantitySoldForProduct(@Param("productId") Long productId);
    
    @Query("SELECT COALESCE(SUM(ti.quantity), 0) FROM TransactionItem ti WHERE ti.product.id = :productId AND ti.transaction.type = 'PURCHASE' AND ti.transaction.status = 'COMPLETED'")
    Integer getTotalQuantityPurchasedForProduct(@Param("productId") Long productId);
}
