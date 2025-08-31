package com.own.agenticpos.repository;

import com.own.agenticpos.entity.Inventory;
import com.own.agenticpos.entity.Product;
import com.own.agenticpos.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    Optional<Inventory> findByProductIdAndLocationId(Long productId, Long locationId);
    
    List<Inventory> findByProductId(Long productId);
    
    List<Inventory> findByLocationId(Long locationId);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.lowStockThreshold")
    List<Inventory> findLowStockItems();
    
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.quantity <= i.lowStockThreshold")
    List<Inventory> findLowStockItemsByProduct(@Param("productId") Long productId);
    
    @Query("SELECT i FROM Inventory i WHERE i.location.id = :locationId AND i.quantity <= i.lowStockThreshold")
    List<Inventory> findLowStockItemsByLocation(@Param("locationId") Long locationId);
    
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalStockForProduct(@Param("productId") Long productId);
    
    @Query("SELECT COALESCE(SUM(i.quantity * i.product.price), 0) FROM Inventory i WHERE i.location.id = :locationId")
    java.math.BigDecimal getTotalInventoryValueByLocation(@Param("locationId") Long locationId);
    
    @Query("SELECT COALESCE(SUM(i.quantity * i.product.price), 0) FROM Inventory i")
    java.math.BigDecimal getTotalInventoryValue();
    
    @Query("SELECT COUNT(DISTINCT i.product.id) FROM Inventory i")
    Long countDistinctProducts();
    
    @Query("SELECT COUNT(DISTINCT i.location.id) FROM Inventory i")
    Long countDistinctLocations();
    
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i")
    Integer getTotalStockUnits();
}
