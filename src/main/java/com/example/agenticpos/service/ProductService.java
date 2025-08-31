package com.example.agenticpos.service;

import com.example.agenticpos.dto.ProductCreateRequest;
import com.example.agenticpos.dto.ProductResponse;
import com.example.agenticpos.dto.ProductUpdateRequest;
import com.example.agenticpos.entity.Product;
import com.example.agenticpos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    /**
     * Get all products with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .map(ProductResponse::from);
    }
    
    /**
     * Create new product
     */
    public ProductResponse createProduct(ProductCreateRequest request) {
        // Check if SKU already exists
        if (request.getSku() != null && productRepository.existsBySkuAndIdNot(request.getSku(), null)) {
            throw new IllegalArgumentException("Product with SKU '" + request.getSku() + "' already exists");
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setCategory(request.getCategory());
        product.setSku(request.getSku());
        product.setActive(request.getActive());
        
        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }
    
    /**
     * Update existing product
     */
    public Optional<ProductResponse> updateProduct(Long id, ProductUpdateRequest request) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .map(product -> {
                    validateSkuForUpdate(request.getSku(), id);
                    updateProductFields(product, request);
                    Product updatedProduct = productRepository.save(product);
                    return ProductResponse.from(updatedProduct);
                });
    }
    
    /**
     * Validate SKU for update operation
     */
    private void validateSkuForUpdate(String sku, Long productId) {
        if (sku != null && productRepository.existsBySkuAndIdNot(sku, productId)) {
            throw new IllegalArgumentException("Product with SKU '" + sku + "' already exists");
        }
    }
    
    /**
     * Update product fields from request
     */
    private void updateProductFields(Product product, ProductUpdateRequest request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getLowStockThreshold() != null) {
            product.setLowStockThreshold(request.getLowStockThreshold());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getSku() != null) {
            product.setSku(request.getSku());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
    }
    
    /**
     * Delete product (soft delete by setting active to false)
     */
    public boolean deleteProduct(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .map(product -> {
                    product.setActive(false);
                    productRepository.save(product);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Search products by keyword
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.searchProducts(keyword, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * Get products with low stock
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        return lowStockProducts.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String category, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByCategory(category, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * Get products by price range
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, 
                                                        int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
        return products.map(ProductResponse::from);
    }
    
    /**
     * Get product by SKU
     */
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .filter(Product::getActive)
                .map(ProductResponse::from);
    }
}
