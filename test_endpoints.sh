#!/bin/bash

# Test script for Product Management API endpoints
BASE_URL="http://localhost:8080/api/v1/products"

echo "Testing Product Management API endpoints..."
echo "=========================================="

echo ""
echo "1. GET /api/v1/products - List all products"
curl -s "${BASE_URL}?page=0&size=5" | jq '.'

echo ""
echo "2. GET /api/v1/products/1 - Get product by ID"
curl -s "${BASE_URL}/1" | jq '.'

echo ""
echo "3. GET /api/v1/products/low-stock - Get low stock products"
curl -s "${BASE_URL}/low-stock" | jq '.'

echo ""
echo "4. GET /api/v1/products/search - Search products"
curl -s "${BASE_URL}/search?keyword=coffee" | jq '.'

echo ""
echo "5. POST /api/v1/products - Create new product"
curl -s -X POST "${BASE_URL}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "A test product for demonstration",
    "price": 29.99,
    "stockQuantity": 15,
    "lowStockThreshold": 5,
    "category": "Test Category",
    "sku": "TEST-001",
    "active": true
  }' | jq '.'

echo ""
echo "6. PUT /api/v1/products/1 - Update product"
curl -s -X PUT "${BASE_URL}/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Coffee Beans",
    "price": 17.99
  }' | jq '.'

echo ""
echo "7. GET /api/v1/products/category/Electronics - Get products by category"
curl -s "${BASE_URL}/category/Electronics" | jq '.'

echo ""
echo "8. GET /api/v1/products/sku/COFFEE-001 - Get product by SKU"
curl -s "${BASE_URL}/sku/COFFEE-001" | jq '.'

echo ""
echo "Testing completed!"
