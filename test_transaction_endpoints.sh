#!/bin/bash

# Transaction Management API Test Script
# This script tests all transaction endpoints in AgenticPOS

BASE_URL="http://localhost:8080/api/v1"
ECHO_ENABLED=true

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_section() {
    if [ "$ECHO_ENABLED" = true ]; then
        echo -e "\n${BLUE}=== $1 ===${NC}"
    fi
}

echo_success() {
    if [ "$ECHO_ENABLED" = true ]; then
        echo -e "${GREEN}✓ $1${NC}"
    fi
}

echo_error() {
    if [ "$ECHO_ENABLED" = true ]; then
        echo -e "${RED}✗ $1${NC}"
    fi
}

echo_warning() {
    if [ "$ECHO_ENABLED" = true ]; then
        echo -e "${YELLOW}⚠ $1${NC}"
    fi
}

make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo_section "$description"
    
    if [ -n "$data" ]; then
        response=$(curl -s -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data" \
            -w "HTTP_STATUS:%{http_code}")
    else
        response=$(curl -s -X $method "$BASE_URL$endpoint" \
            -w "HTTP_STATUS:%{http_code}")
    fi
    
    http_status=$(echo "$response" | grep -o "HTTP_STATUS:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed -E 's/HTTP_STATUS:[0-9]*$//')
    
    if [ "$http_status" -ge 200 ] && [ "$http_status" -lt 300 ]; then
        echo_success "Request successful (HTTP $http_status)"
        if [ "$ECHO_ENABLED" = true ]; then
            echo "$response_body" | jq '.' 2>/dev/null || echo "$response_body"
        fi
    else
        echo_error "Request failed (HTTP $http_status)"
        if [ "$ECHO_ENABLED" = true ]; then
            echo "$response_body" | jq '.' 2>/dev/null || echo "$response_body"
        fi
    fi
    
    echo
}

echo -e "${BLUE}Starting Transaction Management API Tests${NC}"
echo -e "${YELLOW}Make sure the application is running on localhost:8080${NC}\n"

# Test Health Check first
make_request "GET" "/health" "" "Health Check"

# Test Product List (to get valid product IDs)
echo_section "Getting Product List for Test Data"
products_response=$(curl -s "$BASE_URL/products?size=5" | jq -r '.content[0].id' 2>/dev/null)
if [ -n "$products_response" ] && [ "$products_response" != "null" ]; then
    PRODUCT_ID=$products_response
    echo_success "Using Product ID: $PRODUCT_ID"
else
    PRODUCT_ID=1
    echo_warning "Could not fetch product ID, using default: $PRODUCT_ID"
fi

# Test Inventory List (to get valid location IDs)
echo_section "Getting Inventory List for Test Data"
inventory_response=$(curl -s "$BASE_URL/inventory?size=5" | jq -r '.content[0].locationId' 2>/dev/null)
if [ -n "$inventory_response" ] && [ "$inventory_response" != "null" ]; then
    LOCATION_ID=$inventory_response
    echo_success "Using Location ID: $LOCATION_ID"
else
    LOCATION_ID=1
    echo_warning "Could not fetch location ID, using default: $LOCATION_ID"
fi

# Test GET /api/v1/transactions (List all transactions)
make_request "GET" "/transactions" "" "List All Transactions (Default Parameters)"

# Test GET /api/v1/transactions with pagination
make_request "GET" "/transactions?page=0&size=5&sortBy=createdAt&sortDir=desc" "" "List Transactions with Pagination"

# Test POST /api/v1/transactions/sale (Process sale transaction)
sale_data='{
  "locationId": '$LOCATION_ID',
  "customerId": "CUST-001",
  "items": [
    {
      "productId": '$PRODUCT_ID',
      "quantity": 2,
      "unitPrice": 10.50
    }
  ],
  "paymentMethod": "CASH",
  "notes": "Test sale transaction"
}'
make_request "POST" "/transactions/sale" "$sale_data" "Process Sale Transaction"

# Store the transaction ID for later tests
echo_section "Getting Transaction ID for Further Tests"
transaction_response=$(curl -s "$BASE_URL/transactions?size=1" | jq -r '.content[0].id' 2>/dev/null)
if [ -n "$transaction_response" ] && [ "$transaction_response" != "null" ]; then
    TRANSACTION_ID=$transaction_response
    echo_success "Using Transaction ID: $TRANSACTION_ID"
else
    TRANSACTION_ID=1
    echo_warning "Could not fetch transaction ID, using default: $TRANSACTION_ID"
fi

# Test POST /api/v1/transactions/purchase (Process purchase transaction)
purchase_data='{
  "locationId": '$LOCATION_ID',
  "supplierId": "SUPP-001",
  "items": [
    {
      "productId": '$PRODUCT_ID',
      "quantity": 10,
      "unitPrice": 8.00
    }
  ],
  "paymentMethod": "BANK_TRANSFER",
  "notes": "Test purchase transaction"
}'
make_request "POST" "/transactions/purchase" "$purchase_data" "Process Purchase Transaction"

# Test POST /api/v1/transactions/return (Process return transaction)
return_data='{
  "originalTransactionId": '$TRANSACTION_ID',
  "locationId": '$LOCATION_ID',
  "items": [
    {
      "productId": '$PRODUCT_ID',
      "quantity": 1,
      "unitPrice": 10.50,
      "reason": "Defective item"
    }
  ],
  "refundMethod": "CASH",
  "notes": "Test return transaction"
}'
make_request "POST" "/transactions/return" "$return_data" "Process Return Transaction"

# Test GET /api/v1/transactions/{id} (Get transaction details)
make_request "GET" "/transactions/$TRANSACTION_ID" "" "Get Transaction Details by ID"

# Test invalid scenarios
echo_section "Testing Error Scenarios"

# Invalid sale transaction (missing required fields)
invalid_sale_data='{
  "items": [],
  "paymentMethod": "CASH"
}'
make_request "POST" "/transactions/sale" "$invalid_sale_data" "Invalid Sale Transaction (Missing Fields)"

# Invalid return transaction (non-existent original transaction)
invalid_return_data='{
  "originalTransactionId": 99999,
  "locationId": '$LOCATION_ID',
  "items": [
    {
      "productId": '$PRODUCT_ID',
      "quantity": 1,
      "unitPrice": 10.50,
      "reason": "Test"
    }
  ],
  "refundMethod": "CASH"
}'
make_request "POST" "/transactions/return" "$invalid_return_data" "Invalid Return Transaction (Non-existent Original)"

# Invalid transaction ID lookup
make_request "GET" "/transactions/99999" "" "Get Non-existent Transaction"

# Invalid purchase transaction (negative quantity)
invalid_purchase_data='{
  "locationId": '$LOCATION_ID',
  "supplierId": "SUPP-001",
  "items": [
    {
      "productId": '$PRODUCT_ID',
      "quantity": -5,
      "unitPrice": 8.00
    }
  ],
  "paymentMethod": "CASH"
}'
make_request "POST" "/transactions/purchase" "$invalid_purchase_data" "Invalid Purchase Transaction (Negative Quantity)"

echo_section "Transaction API Test Summary"
echo_success "All transaction endpoint tests completed!"
echo_warning "Check the responses above for any failures"
echo -e "${BLUE}Transaction types tested: SALE, PURCHASE, RETURN${NC}"
echo -e "${BLUE}Validation tests: Missing fields, invalid data, non-existent records${NC}"
