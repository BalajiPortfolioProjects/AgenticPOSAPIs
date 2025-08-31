package com.example.agenticpos.repository;

import com.example.agenticpos.entity.StockMovement;
import com.example.agenticpos.entity.StockMovement.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    Page<StockMovement> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    List<StockMovement> findByLocationIdOrderByCreatedAtDesc(Long locationId);
    
    List<StockMovement> findByMovementTypeOrderByCreatedAtDesc(MovementType movementType);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId AND sm.location.id = :locationId ORDER BY sm.createdAt DESC")
    List<StockMovement> findByProductAndLocationOrderByCreatedAtDesc(@Param("productId") Long productId, @Param("locationId") Long locationId);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.createdAt BETWEEN :startDate AND :endDate ORDER BY sm.createdAt DESC")
    List<StockMovement> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId AND sm.createdAt BETWEEN :startDate AND :endDate ORDER BY sm.createdAt DESC")
    List<StockMovement> findByProductAndDateRangeOrderByCreatedAtDesc(@Param("productId") Long productId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
