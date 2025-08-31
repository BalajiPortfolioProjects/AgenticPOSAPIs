#!/bin/bash

# Inventory API Test Script
BASE_URL="http://localhost:8080/api/v1"

echo "=== Testing Inventory Management Endpoints ==="
echo ""

# Test 1: Get inventory overview
echo "1. Testing GET /api/v1/inventory - Get inventory overview"
curl -X GET "${BASE_URL}/inventory" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

# Test 2: Get all locations
echo "2. Testing GET /api/v1/locations - Get all locations"
curl -X GET "${BASE_URL}/locations" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

# Test 3: Create a new location
echo "3. Testing POST /api/v1/locations - Create new location"
curl -X POST "${BASE_URL}/locations" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "East Branch",
    "address": "789 East Avenue",
    "city": "Springfield",
    "state": "IL",
    "zipCode": "62704"
  }' | jq '.'
echo ""
echo ""

# Test 4: Get inventory for specific product (assuming product ID 1 exists)
echo "4. Testing GET /api/v1/inventory/{productId} - Get inventory for product ID 1"
curl -X GET "${BASE_URL}/inventory/1" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

# Test 5: Adjust stock levels
echo "5. Testing POST /api/v1/inventory/adjust - Adjust stock (increase by 10)"
curl -X POST "${BASE_URL}/inventory/adjust" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "locationId": 1,
    "adjustmentQuantity": 10,
    "movementType": "ADJUSTMENT_IN",
    "reference": "Manual adjustment",
    "notes": "Adding extra stock for promotion",
    "createdBy": "admin"
  }' | jq '.'
echo ""
echo ""

# Test 6: Transfer stock between locations
echo "6. Testing POST /api/v1/inventory/transfer - Transfer stock between locations"
curl -X POST "${BASE_URL}/inventory/transfer" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "fromLocationId": 2,
    "toLocationId": 1,
    "quantity": 5,
    "reference": "Stock rebalancing",
    "notes": "Moving stock from warehouse to main store",
    "createdBy": "admin"
  }' | jq '.'
echo ""
echo ""

# Test 7: Get stock movements with pagination
echo "7. Testing GET /api/v1/inventory/movements - Get stock movements (first 5)"
curl -X GET "${BASE_URL}/inventory/movements?page=0&size=5&sortBy=createdAt&sortDir=desc" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

# Test 8: Get stock movements for specific product
echo "8. Testing GET /api/v1/inventory/movements/product/{productId} - Get movements for product ID 1"
curl -X GET "${BASE_URL}/inventory/movements/product/1" \
  -H "Content-Type: application/json" | jq '.'
echo ""
echo ""

# Test 9: Error case - insufficient stock
echo "9. Testing error case - try to adjust stock below zero"
curl -X POST "${BASE_URL}/inventory/adjust" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "locationId": 1,
    "adjustmentQuantity": -1000,
    "movementType": "ADJUSTMENT_OUT",
    "reference": "Test error case",
    "notes": "This should fail due to insufficient stock",
    "createdBy": "admin"
  }' | jq '.'
echo ""
echo ""

# Test 10: Error case - invalid transfer (same location)
echo "10. Testing error case - transfer to same location"
curl -X POST "${BASE_URL}/inventory/transfer" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "fromLocationId": 1,
    "toLocationId": 1,
    "quantity": 5,
    "reference": "Test error case",
    "notes": "This should fail - same location",
    "createdBy": "admin"
  }' | jq '.'
echo ""
echo ""

echo "=== Inventory API Test Complete ==="
