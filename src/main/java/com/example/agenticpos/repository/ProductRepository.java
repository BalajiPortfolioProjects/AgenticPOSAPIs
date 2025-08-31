package com.example.agenticpos.repository;

import com.example.agenticpos.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find products by name containing keyword (case insensitive)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    // Find products by category
    Page<Product> findByCategory(String category, Pageable pageable);
    
    // Find products by SKU
    Optional<Product> findBySku(String sku);
    
    // Find active products only
    Page<Product> findByActiveTrue(Pageable pageable);
    
    // Find products with low stock
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold AND p.active = true")
    List<Product> findLowStockProducts();
    
    // Search products by name, description, or category
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "p.active = true")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // Check if SKU exists (excluding current product ID)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.sku = :sku AND (:id IS NULL OR p.id != :id)")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("id") Long id);
    
    // Find products by price range
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    Page<Product> findByPriceBetween(@Param("minPrice") java.math.BigDecimal minPrice, 
                                   @Param("maxPrice") java.math.BigDecimal maxPrice, 
                                   Pageable pageable);
}
