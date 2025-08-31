# Inventory Management API Documentation

This document describes the inventory management endpoints for the AgenticPOS application.

## Endpoints Overview

### Inventory Management

#### 1. Get Inventory Overview
**GET** `/api/v1/inventory`

Returns a comprehensive overview of the inventory including:
- Total products and locations
- Total stock units and value
- Low stock items
- Location summaries

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
  "adjustmentQuantity": 10,
  "movementType": "ADJUSTMENT_IN",
  "reference": "Manual adjustment",
  "notes": "Adding extra stock for promotion",
  "createdBy": "admin"
}
```

**Movement Types:**
- `ADJUSTMENT_IN` - Increase stock
- `ADJUSTMENT_OUT` - Decrease stock
- `DAMAGE` - Damage/Loss
- `RETURN` - Customer return

**Response:** Returns updated inventory record.

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

**Response:** Returns array with updated inventory records for both locations.

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

### Location Management

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

The API returns appropriate HTTP status codes and error messages:

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

1. **Stock Adjustments:**
   - Cannot reduce stock below zero
   - All adjustments create stock movement records

2. **Stock Transfers:**
   - Cannot transfer to the same location
   - Must have sufficient stock at source location
   - Creates movement records for both source and destination

3. **Location Management:**
   - Location names must be unique
   - Cannot delete locations with existing inventory

4. **Inventory Tracking:**
   - Each product can have inventory at multiple locations
   - Low stock alerts based on configurable thresholds
   - Reserved quantity tracked separately from available quantity

## Sample Data

The application loads sample data on startup including:
- 3 locations (Main Store, Warehouse, Online Store)
- 5 products across various categories
- Initial inventory distributed across locations
- Initial stock movement records
