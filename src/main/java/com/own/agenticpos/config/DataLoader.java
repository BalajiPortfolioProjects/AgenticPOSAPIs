package com.own.agenticpos.config;

import com.own.agenticpos.entity.Product;
import com.own.agenticpos.entity.Location;
import com.own.agenticpos.entity.Inventory;
import com.own.agenticpos.entity.StockMovement;
import com.own.agenticpos.repository.ProductRepository;
import com.own.agenticpos.repository.LocationRepository;
import com.own.agenticpos.repository.InventoryRepository;
import com.own.agenticpos.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {
    
    private static final String SPRINGFIELD_CITY = "Springfield";
    private static final String ILLINOIS_STATE = "IL";
    
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Only load data if no products exist
        if (productRepository.count() == 0) {
            loadSampleData();
        }
    }
    
    private void loadSampleData() {
        log.info("Loading sample data...");
        
        // Create locations first
        List<Location> locations = createSampleLocations();
        
        // Create sample products
        List<Product> products = createSampleProducts();
        
        // Create inventory records
        createSampleInventory(products, locations);
        
        log.info("Sample data loaded successfully!");
    }
    
    private List<Location> createSampleLocations() {
        Location mainStore = Location.builder()
                .name("Main Store")
                .address("123 Main Street")
                .city(SPRINGFIELD_CITY)
                .state(ILLINOIS_STATE)
                .zipCode("62701")
                .active(true)
                .build();
        
        Location warehouse = Location.builder()
                .name("Warehouse")
                .address("456 Industrial Blvd")
                .city(SPRINGFIELD_CITY)
                .state(ILLINOIS_STATE)
                .zipCode("62702")
                .active(true)
                .build();
        
        Location onlineStore = Location.builder()
                .name("Online Store")
                .address("Virtual Location")
                .city(SPRINGFIELD_CITY)
                .state(ILLINOIS_STATE)
                .zipCode("62703")
                .active(true)
                .build();
        
        mainStore = locationRepository.save(mainStore);
        warehouse = locationRepository.save(warehouse);
        onlineStore = locationRepository.save(onlineStore);
        
        log.info("Created {} locations", 3);
        return List.of(mainStore, warehouse, onlineStore);
    }
    
    private List<Product> createSampleProducts() {
        // Create sample products
        Product coffee = new Product();
        coffee.setName("Premium Coffee Beans");
        coffee.setDescription("High-quality arabica coffee beans from Colombia");
        coffee.setPrice(new BigDecimal("15.99"));
        coffee.setStockQuantity(50);
        coffee.setLowStockThreshold(10);
        coffee.setCategory("Beverages");
        coffee.setSku("COFFEE-001");
        coffee.setActive(true);
        
        Product laptop = new Product();
        laptop.setName("Business Laptop");
        laptop.setDescription("High-performance laptop for business use");
        laptop.setPrice(new BigDecimal("899.99"));
        laptop.setStockQuantity(5);
        laptop.setLowStockThreshold(3);
        laptop.setCategory("Electronics");
        laptop.setSku("LAPTOP-001");
        laptop.setActive(true);
        
        Product pen = new Product();
        pen.setName("Blue Ink Pen");
        pen.setDescription("Professional blue ink ballpoint pen");
        pen.setPrice(new BigDecimal("2.50"));
        pen.setStockQuantity(100);
        pen.setLowStockThreshold(20);
        pen.setCategory("Office Supplies");
        pen.setSku("PEN-001");
        pen.setActive(true);
        
        Product notebook = new Product();
        notebook.setName("Spiral Notebook");
        notebook.setDescription("A4 size spiral-bound notebook with lined pages");
        notebook.setPrice(new BigDecimal("5.99"));
        notebook.setStockQuantity(8);
        notebook.setLowStockThreshold(15);
        notebook.setCategory("Office Supplies");
        notebook.setSku("NOTEBOOK-001");
        notebook.setActive(true);
        
        Product smartphone = new Product();
        smartphone.setName("Smartphone");
        smartphone.setDescription("Latest generation smartphone with advanced features");
        smartphone.setPrice(new BigDecimal("699.99"));
        smartphone.setStockQuantity(2);
        smartphone.setLowStockThreshold(5);
        smartphone.setCategory("Electronics");
        smartphone.setSku("PHONE-001");
        smartphone.setActive(true);
        
        // Save all products
        coffee = productRepository.save(coffee);
        laptop = productRepository.save(laptop);
        pen = productRepository.save(pen);
        notebook = productRepository.save(notebook);
        smartphone = productRepository.save(smartphone);
        
        log.info("Created {} products", 5);
        return List.of(coffee, laptop, pen, notebook, smartphone);
    }
    
    private void createSampleInventory(List<Product> products, List<Location> locations) {
        Location mainStore = locations.get(0);
        Location warehouse = locations.get(1);
        Location onlineStore = locations.get(2);
        
        // Create inventory for each product at each location
        for (Product product : products) {
            // Main Store - 40% of total stock
            int mainStoreStock = (int) (product.getStockQuantity() * 0.4);
            createInventoryRecord(product, mainStore, mainStoreStock);
            
            // Warehouse - 50% of total stock
            int warehouseStock = (int) (product.getStockQuantity() * 0.5);
            createInventoryRecord(product, warehouse, warehouseStock);
            
            // Online Store - remaining stock
            int onlineStock = product.getStockQuantity() - mainStoreStock - warehouseStock;
            createInventoryRecord(product, onlineStore, onlineStock);
        }
        
        log.info("Created inventory records for {} products across {} locations", 
                products.size(), locations.size());
    }
    
    private void createInventoryRecord(Product product, Location location, int quantity) {
        Inventory inventory = Inventory.builder()
                .product(product)
                .location(location)
                .quantity(quantity)
                .reservedQuantity(0)
                .lowStockThreshold(product.getLowStockThreshold())
                .build();
        
        inventoryRepository.save(inventory);
        
        // Create initial stock movement
        StockMovement movement = StockMovement.builder()
                .product(product)
                .location(location)
                .movementType(StockMovement.MovementType.INITIAL_STOCK)
                .quantity(quantity)
                .previousQuantity(0)
                .newQuantity(quantity)
                .reference("Initial stock load")
                .notes("Sample data initialization")
                .createdBy("System")
                .build();
        
        stockMovementRepository.save(movement);
    }
}
