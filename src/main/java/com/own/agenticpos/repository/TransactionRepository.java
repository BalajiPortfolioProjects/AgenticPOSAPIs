package com.own.agenticpos.repository;

import com.own.agenticpos.entity.Transaction;
import com.own.agenticpos.entity.Transaction.TransactionType;
import com.own.agenticpos.entity.Transaction.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Page<Transaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<Transaction> findByTypeOrderByCreatedAtDesc(TransactionType type);
    
    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
    
    List<Transaction> findByLocationIdOrderByCreatedAtDesc(Long locationId);
    
    Optional<Transaction> findByTransactionNumber(String transactionNumber);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.type = :type AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(@Param("type") TransactionType type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.location.id = :locationId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByLocationAndDateRangeOrderByCreatedAtDesc(@Param("locationId") Long locationId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.type = :type AND t.status = 'COMPLETED'")
    java.math.BigDecimal getTotalAmountByType(@Param("type") TransactionType type);
    
    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t WHERE t.type = :type AND t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalAmountByTypeAndDateRange(@Param("type") TransactionType type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = :type AND t.status = 'COMPLETED'")
    Long countByTypeAndCompleted(@Param("type") TransactionType type);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = :type AND t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    Long countByTypeAndCompletedAndDateRange(@Param("type") TransactionType type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
