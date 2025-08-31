# AgenticPOS API Documentation

This document provides comprehensive documentation for all APIs available in the AgenticPOS application, including product management, inventory management, transaction management, and location management.

## Table of Contents
- [Product Management API](#product-management-api)
- [Inventory Management API](#inventory-management-api)
- [Transaction Management API](#transaction-management-api)
- [Location Management API](#location-management-api)
- [Error Handling](#error-handling)
- [Business Rules](#business-rules)
- [Testing](#testing)

## Product Management API

### Base URL
All product endpoints are prefixed with: `/api/v1/products`

### Endpoints

#### 1. List All Products (Paginated)
**GET** `/api/v1/products`

Returns a paginated list of all products.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Page size
- `sortBy` (default: "name") - Sort field
- `sortDir` (default: "asc") - Sort direction (asc/desc)

**Example Request:**
```bash
GET /api/v1/products?page=0&size=3&sortBy=name&sortDir=asc
```

#### 2. Get Product by ID
**GET** `/api/v1/products/{id}`

Returns a specific product by its ID.

**Path Parameters:**
- `id` - Product ID

**Example Request:**
```bash
GET /api/v1/products/1
```

#### 3. Get Low Stock Products
**GET** `/api/v1/products/low-stock`

Returns products that are below their low stock threshold.

#### 4. Search Products
**GET** `/api/v1/products/search`

Search products by keyword.

**Query Parameters:**
- `keyword` - Search term
- `page` (default: 0) - Page number
- `size` (default: 20) - Page size

**Example Request:**
```bash
GET /api/v1/products/search?keyword=coffee&page=0&size=5
```

#### 5. Get Products by Category
**GET** `/api/v1/products/category/{category}`

Returns products filtered by category.

**Path Parameters:**
- `category` - Product category

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Page size

**Example Request:**
```bash
GET /api/v1/products/category/Electronics?page=0&size=5
```

#### 6. Get Product by SKU
**GET** `/api/v1/products/sku/{sku}`

Returns a product by its SKU.

**Path Parameters:**
- `sku` - Product SKU

**Example Request:**
```bash
GET /api/v1/products/sku/COFFEE-001
```

#### 7. Get Products by Price Range
**GET** `/api/v1/products/price-range`

Returns products within a specified price range.

**Query Parameters:**
- `minPrice` - Minimum price
- `maxPrice` - Maximum price
- `page` (default: 0) - Page number
- `size` (default: 20) - Page size

**Example Request:**
```bash
GET /api/v1/products/price-range?minPrice=5.00&maxPrice=20.00&page=0&size=5
```

#### 8. Create Product
**POST** `/api/v1/products`

Creates a new product.

**Request Body:**
```json
{
  "name": "Test Product API",
  "description": "A test product created via API",
  "price": 29.99,
  "stockQuantity": 15,
  "lowStockThreshold": 5,
  "category": "Test Category",
  "sku": "TEST-API-001",
  "active": true
}
```

#### 9. Update Product
**PUT** `/api/v1/products/{id}`

Updates an existing product.

**Path Parameters:**
- `id` - Product ID

**Request Body:**
```json
{
  "name": "Premium Coffee Beans - Updated",
  "price": 18.99,
  "stockQuantity": 75
}
```

## Inventory Management API

### Base URL
All inventory endpoints are prefixed with: `/api/v1/inventory`

### Endpoints

#### 1. Get Inventory Overview
**GET** `/api/v1/inventory`

Returns a comprehensive overview of the inventory including total products, locations, stock units, value, and low stock items.

**Response Example:**
```json
{
  "totalProducts": 5,
  "totalLocations": 3,
  "totalStockUnits": 165,
  "lowStockItems": 2,
  "totalInventoryValue": 1234.56,
  "locationSummaries": [
    {
      "locationId": 1,
      "locationName": "Main Store",
      "totalItems": 5,
      "lowStockItems": 1,
      "inventoryValue": 456.78
    }
  ],
  "lowStockItemsList": [
    {
      "productId": 4,
      "productName": "Spiral Notebook",
      "productSku": "NOTEBOOK-001",
      "locationId": 1,
      "locationName": "Main Store",
      "currentStock": 3,
      "threshold": 15,
      "suggestedReorder": 30
    }
  ]
}
```

#### 2. Get Inventory for Specific Product
**GET** `/api/v1/inventory/{productId}`

Returns inventory information for a specific product across all locations.

**Path Parameters:**
- `productId` - Product ID

**Response Example:**
```json
[
  {
    "id": 1,
    "productId": 1,
    "productName": "Premium Coffee Beans",
    "productSku": "COFFEE-001",
    "locationId": 1,
    "locationName": "Main Store",
    "quantity": 20,
    "reservedQuantity": 0,
    "availableQuantity": 20,
    "lowStockThreshold": 10,
    "isLowStock": false,
    "updatedAt": "2025-08-31T10:30:00"
  }
]
```

#### 3. Adjust Stock Levels
**POST** `/api/v1/inventory/adjust`

Adjusts stock levels for a product at a specific location.

**Request Body:**
```json
{
  "productId": 1,
  "locationId": 1,
  "adjustmentType": "INCREASE",
  "quantity": 10,
  "reason": "Stock replenishment",
  "notes": "Weekly stock delivery",
  "createdBy": "admin"
}
```

#### 4. Transfer Stock Between Locations
**POST** `/api/v1/inventory/transfer`

Transfers stock from one location to another.

**Request Body:**
```json
{
  "productId": 1,
  "fromLocationId": 2,
  "toLocationId": 1,
  "quantity": 5,
  "reference": "Stock rebalancing",
  "notes": "Moving stock from warehouse to main store",
  "createdBy": "admin"
}
```

#### 5. Get Stock Movements History
**GET** `/api/v1/inventory/movements`

Returns paginated list of all stock movements.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Page size
- `sortBy` (default: "createdAt") - Sort field
- `sortDir` (default: "desc") - Sort direction

**Response Example:**
```json
{
  "content": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Premium Coffee Beans",
      "productSku": "COFFEE-001",
      "locationId": 1,
      "locationName": "Main Store",
      "movementType": "TRANSFER_IN",
      "movementDescription": "Transfer In",
      "quantity": 5,
      "previousQuantity": 15,
      "newQuantity": 20,
      "reference": "Stock rebalancing",
      "notes": "Moving from warehouse",
      "fromLocationId": 2,
      "fromLocationName": "Warehouse",
      "toLocationId": 1,
      "toLocationName": "Main Store",
      "createdBy": "admin",
      "createdAt": "2025-08-31T10:30:00"
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### 6. Get Stock Movements for Product
**GET** `/api/v1/inventory/movements/product/{productId}`

Returns all stock movements for a specific product.

**Path Parameters:**
- `productId` - Product ID

## Transaction Management API

### Base URL
All transaction endpoints are prefixed with: `/api/v1/transactions`

### Endpoints

#### 1. List All Transactions
**GET** `/api/v1/transactions`

Returns a paginated list of all transactions.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Page size
- `sortBy` (default: "createdAt") - Sort field
- `sortDir` (default: "desc") - Sort direction

**Example Request:**
```bash
GET /api/v1/transactions?page=0&size=5&sortBy=createdAt&sortDir=desc
```

#### 2. Process Sale Transaction
**POST** `/api/v1/transactions/sale`

Processes a sale transaction.

#### 3. Get Transaction Details
**GET** `/api/v1/transactions/{id}`

Returns details for a specific transaction.

**Path Parameters:**
- `id` - Transaction ID

**Example Request:**
```bash
GET /api/v1/transactions/1
```

## Location Management API

### Base URL
All location endpoints are prefixed with: `/api/v1/locations`

### Endpoints

#### 1. Get All Locations
**GET** `/api/v1/locations`

Returns list of all active locations.

**Response Example:**
```json
[
  {
    "id": 1,
    "name": "Main Store",
    "address": "123 Main Street",
    "city": "Springfield",
    "state": "IL",
    "zipCode": "62701",
    "active": true,
    "createdAt": "2025-08-31T09:00:00",
    "updatedAt": "2025-08-31T09:00:00"
  }
]
```

#### 2. Get Location by ID
**GET** `/api/v1/locations/{id}`

Returns specific location by ID.

**Path Parameters:**
- `id` - Location ID

#### 3. Create Location
**POST** `/api/v1/locations`

Creates a new location.

**Request Body:**
```json
{
  "name": "East Branch",
  "address": "789 East Avenue",
  "city": "Springfield",
  "state": "IL",
  "zipCode": "62704"
}
```

## Error Handling

The API returns appropriate HTTP status codes and error messages for different scenarios.

### Common Error Responses

#### 400 Bad Request - Validation Error
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed for one or more fields",
  "timestamp": "2025-08-31T10:30:00",
  "details": {
    "productId": "Product ID is required",
    "quantity": "Quantity must be at least 1"
  }
}
```

#### 400 Bad Request - Insufficient Stock
```json
{
  "code": "INSUFFICIENT_STOCK",
  "message": "Insufficient stock. Current quantity: 5, Requested: 10",
  "timestamp": "2025-08-31T10:30:00",
  "details": null
}
```

#### 400 Bad Request - Invalid Transfer
```json
{
  "code": "INVALID_TRANSFER",
  "message": "Cannot transfer to the same location",
  "timestamp": "2025-08-31T10:30:00",
  "details": null
}
```

#### 400 Bad Request - Duplicate Location
```json
{
  "code": "DUPLICATE_LOCATION",
  "message": "Location with name 'Main Store' already exists",
  "timestamp": "2025-08-31T10:30:00",
  "details": null
}
```

#### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "Resource not found",
  "timestamp": "2025-08-31T10:30:00",
  "details": null
}
```

## Business Rules

### Stock Adjustments
- Cannot reduce stock below zero
- All adjustments create stock movement records

### Stock Transfers
- Cannot transfer to the same location
- Must have sufficient stock at source location
- Creates movement records for both source and destination

### Location Management
- Location names must be unique
- Cannot delete locations with existing inventory

### Inventory Tracking
- Each product can have inventory at multiple locations
- Low stock alerts based on configurable thresholds
- Reserved quantity tracked separately from available quantity

## Sample Data

The application loads sample data on startup including:
- 3 locations (Main Store, Warehouse, Online Store)
- 5 products across various categories
- Initial inventory distributed across locations
- Initial stock movement records

## Testing

### Running Tests
Use the test scripts provided in the workspace:

1. **All endpoints**: Run commands from [api_test_commands.txt](api_test_commands.txt)
2. **Inventory endpoints**: Use [test_inventory_endpoints.sh](test_inventory_endpoints.sh)
3. **Transaction endpoints**: Use [test_transaction_endpoints.sh](test_transaction_endpoints.sh)
4. **General endpoints**: Use [test_endpoints.sh](test_endpoints.sh)

### Example Test Commands

Test product creation with validation error:
```bash
curl -s -X POST "http://localhost:8080/api/v1/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "price": -10.50,
    "stockQuantity": -5
  }' | python3 -m json.tool
```

Test 404 for non-existent product:
```bash
curl -s "http://localhost:8080/api/v1/products/999" | python3 -m json.tool
```

## Application Configuration

The application uses Spring Boot and can be configured via [application.properties](src/main/resources/application.properties). The application runs on port 8080 by default.

## Build and Deployment

Build the application using Maven:
```bash
mvn clean package
```

The built JAR file will be available at [target/agentic-pos-0.0.1-SNAPSHOT.jar](target/agentic-pos-0.0.1-SNAPSHOT.jar).
