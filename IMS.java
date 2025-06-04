import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

class Product implements Serializable {
    private final int id;
    private String name;
    private int quantity;
    private double price;
    private String category;
    private Date lastRestockDate;

    public Product(int id, String name, int quantity, double price, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.lastRestockDate = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public Date getLastRestockDate() { return lastRestockDate; }
    
    public void setName(String name) { this.name = name; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        this.lastRestockDate = new Date();
    }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return String.format("| %-4d | %-20s | %-8d | $%-8.2f | %-15s | %-12s |", 
                id, name, quantity, price, category, sdf.format(lastRestockDate));
    }
}

class Sale implements Serializable {
    private final int productId;
    private final String productName;
    private final int quantitySold;
    private final double totalAmount;
    private final Date saleDate;

    public Sale(int productId, String productName, int quantitySold, double totalAmount) {
        this.productId = productId;
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.totalAmount = totalAmount;
        this.saleDate = new Date();
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantitySold() { return quantitySold; }
    public double getTotalAmount() { return totalAmount; }
    public Date getSaleDate() { return saleDate; }
}

class FileManager {
    private static final String INVENTORY_FILE = "inventory.dat";
    private static final String SALES_FILE = "sales.dat";

    public static void saveInventory(List<Product> inventory) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INVENTORY_FILE))) {
            oos.writeObject(inventory);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Product> loadInventory() throws IOException, ClassNotFoundException {
        File file = new File(INVENTORY_FILE);
        if (!file.exists()) return new ArrayList<>();
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(INVENTORY_FILE))) {
            return (List<Product>) ois.readObject();
        }
    }

    public static void saveSales(List<Sale> sales) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SALES_FILE))) {
            oos.writeObject(sales);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Sale> loadSales() throws IOException, ClassNotFoundException {
        File file = new File(SALES_FILE);
        if (!file.exists()) return new ArrayList<>();
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SALES_FILE))) {
            return (List<Sale>) ois.readObject();
        }
    }
}

public class IMS {
    private static List<Product> inventory = new ArrayList<>();
    private static List<Sale> sales = new ArrayList<>();
    private static int nextId = 1;
    private static final Scanner scanner = new Scanner(System.in);
    private static final int LOW_STOCK_THRESHOLD = 10;

    public static void main(String[] args) {
        initializeSystem();
        
        while (true) {
            printMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1: addProduct(); break;
                case 2: updateProduct(); break;
                case 3: deleteProduct(); break;
                case 4: viewAllProducts(); break;
                case 5: searchProduct(); break;
                case 6: recordSale(); break;
                case 7: generateInventoryReport(); break;
                case 8: generateSalesReport(); break;
                case 9: viewLowStock(); break;
                case 10: 
                    saveSystemState();
                    System.out.println("Exiting system...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void initializeSystem() {
        try {
            inventory = FileManager.loadInventory();
            sales = FileManager.loadSales();
            
            // Find the highest ID for nextId
            nextId = inventory.stream()
                .mapToInt(Product::getId)
                .max()
                .orElse(0) + 1;
                
            System.out.println("System initialized successfully!");
            System.out.println("Loaded " + inventory.size() + " products and " + sales.size() + " sales records.");
        } catch (Exception e) {
            System.out.println("Error initializing system: " + e.getMessage());
            System.out.println("Starting with empty inventory.");
        }
    }

    private static void saveSystemState() {
        try {
            FileManager.saveInventory(inventory);
            FileManager.saveSales(sales);
            System.out.println("System state saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving system state: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
        System.out.println("1.  Add New Product");
        System.out.println("2.  Update Product");
        System.out.println("3.  Delete Product");
        System.out.println("4.  View All Products");
        System.out.println("5.  Search Product");
        System.out.println("6.  Record Sale");
        System.out.println("7.  Generate Inventory Report");
        System.out.println("8.  Generate Sales Report");
        System.out.println("9.  View Low Stock Alerts");
        System.out.println("10. Exit");
        System.out.println("======================================");
    }

    private static void addProduct() {
        System.out.println("\n--- Add New Product ---");
        String name = getStringInput("Enter product name: ");
        int quantity = getIntInput("Enter quantity: ");
        double price = getDoubleInput("Enter price: $");
        String category = getStringInput("Enter category: ");
        
        Product product = new Product(nextId++, name, quantity, price, category);
        inventory.add(product);
        System.out.println("Product added successfully! ID: " + product.getId());
    }

    private static void updateProduct() {
        System.out.println("\n--- Update Product ---");
        int id = getIntInput("Enter product ID to update: ");
        Product product = findProductById(id);
        
        if (product == null) {
            System.out.println("Product not found!");
            return;
        }
        
        System.out.println("Current details:");
        printHeader();
        System.out.println(product);
        
        System.out.println("\nEnter new details (leave blank to keep current):");
        String name = getStringInput("Name [" + product.getName() + "]: ");
        String quantityInput = getStringInput("Quantity [" + product.getQuantity() + "]: ");
        String priceInput = getStringInput("Price [$" + product.getPrice() + "]: ");
        String category = getStringInput("Category [" + product.getCategory() + "]: ");
        
        if (!name.isEmpty()) product.setName(name);
        if (!quantityInput.isEmpty()) product.setQuantity(Integer.parseInt(quantityInput));
        if (!priceInput.isEmpty()) product.setPrice(Double.parseDouble(priceInput));
        if (!category.isEmpty()) product.setCategory(category);
        
        System.out.println("Product updated successfully!");
    }

    private static void deleteProduct() {
        System.out.println("\n--- Delete Product ---");
        int id = getIntInput("Enter product ID to delete: ");
        Product product = findProductById(id);
        
        if (product == null) {
            System.out.println("Product not found!");
            return;
        }
        
        inventory.remove(product);
        System.out.println("Product deleted successfully!");
    }

    private static void viewAllProducts() {
        System.out.println("\n--- All Products ---");
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty!");
            return;
        }
        
        printHeader();
        inventory.forEach(System.out::println);
        printFooter();
    }

    private static void searchProduct() {
        System.out.println("\n--- Search Product ---");
        System.out.println("1. Search by ID");
        System.out.println("2. Search by Name");
        System.out.println("3. Search by Category");
        int searchType = getIntInput("Choose search type: ");
        
        switch (searchType) {
            case 1:
                int id = getIntInput("Enter product ID: ");
                Product product = findProductById(id);
                if (product == null) {
                    System.out.println("Product not found!");
                } else {
                    printHeader();
                    System.out.println(product);
                    printFooter();
                }
                break;
            case 2:
                String name = getStringInput("Enter product name: ");
                List<Product> nameResults = inventory.stream()
                    .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
                displaySearchResults(nameResults);
                break;
            case 3:
                String category = getStringInput("Enter category: ");
                List<Product> categoryResults = inventory.stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
                displaySearchResults(categoryResults);
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void recordSale() {
        System.out.println("\n--- Record Sale ---");
        int id = getIntInput("Enter product ID: ");
        Product product = findProductById(id);
        
        if (product == null) {
            System.out.println("Product not found!");
            return;
        }
        
        int available = product.getQuantity();
        System.out.println("Available quantity: " + available);
        int quantity = getIntInput("Enter quantity sold: ");
        
        if (quantity <= 0) {
            System.out.println("Invalid quantity!");
            return;
        }
        
        if (quantity > available) {
            System.out.println("Not enough stock available!");
            return;
        }
        
        double total = quantity * product.getPrice();
        Sale sale = new Sale(product.getId(), product.getName(), quantity, total);
        sales.add(sale);
        
        product.setQuantity(available - quantity);
        System.out.printf("Sale recorded successfully! Total: $%.2f\n", total);
    }

    private static void generateInventoryReport() {
        System.out.println("\n--- Inventory Report ---");
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty!");
            return;
        }
        
        printHeader();
        double totalValue = 0;
        for (Product product : inventory) {
            System.out.println(product);
            totalValue += product.getPrice() * product.getQuantity();
        }
        printFooter();
        
        System.out.printf("Total Products: %d | Total Inventory Value: $%.2f\n", 
                inventory.size(), totalValue);
    }

    private static void generateSalesReport() {
        System.out.println("\n--- Sales Report ---");
        if (sales.isEmpty()) {
            System.out.println("No sales records found!");
            return;
        }
        
        System.out.println("----------------------------------------------------------------");
        System.out.println("| Product ID | Product Name      | Qty Sold | Total Amount | Date       |");
        System.out.println("----------------------------------------------------------------");
        
        double totalSales = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Sale sale : sales) {
            System.out.printf("| %-10d | %-18s | %-8d | $%-11.2f | %-10s |\n",
                    sale.getProductId(),
                    sale.getProductName(),
                    sale.getQuantitySold(),
                    sale.getTotalAmount(),
                    sdf.format(sale.getSaleDate()));
            totalSales += sale.getTotalAmount();
        }
        System.out.println("----------------------------------------------------------------");
        System.out.printf("Total Sales: $%.2f\n", totalSales);
    }

    private static void viewLowStock() {
        System.out.println("\n--- Low Stock Alerts ---");
        List<Product> lowStock = inventory.stream()
            .filter(p -> p.getQuantity() <= LOW_STOCK_THRESHOLD)
            .collect(Collectors.toList());
        
        if (lowStock.isEmpty()) {
            System.out.println("No products with low stock!");
            return;
        }
        
        printHeader();
        lowStock.forEach(System.out::println);
        printFooter();
    }

    private static Product findProductById(int id) {
        return inventory.stream()
            .filter(p -> p.getId() == id)
            .findFirst()
            .orElse(null);
    }

    private static void printHeader() {
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println("| ID   | Name                 | Quantity | Price    | Category       | Last Restock |");
        System.out.println("-------------------------------------------------------------------------------------");
    }

    private static void printFooter() {
        System.out.println("-------------------------------------------------------------------------------------");
    }

    private static void displaySearchResults(List<Product> results) {
        if (results.isEmpty()) {
            System.out.println("No products found!");
            return;
        }
        
        printHeader();
        results.forEach(System.out::println);
        printFooter();
    }

    // Helper methods for input validation
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid integer.");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }
}