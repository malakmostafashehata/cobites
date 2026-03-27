package backend;

import java.time.LocalDate;

// Order class represents a food order made by a donor to a charity
public class Order {
    private String id;                 // Unique identifier for the order
    private String itemName;           // Name of the food item
    private int quantity;              // Quantity of the item being ordered
    private LocalDate date;            // Date the order was placed
    private String status;             // Status of the order (e.g., Pending, Completed)
    private String donorAddress;       // Address of the donor
    private String charityAddress;     // Address of the charity receiving the order
    private String charityPhone;       // Phone number of the charity
    private String donorPhone;         // Phone number of the donor
    private String donorUsername;      // Username of the donor
    private FoodType type;             // Type/category of the food item
    private String charityUsername;    // Username of the charity
    private String deliveryUserName;    // Username of the charity

    // ===== Constructor =====
    // Initializes a new Order with all relevant details
    public Order(String id, String charityUsername, String itemName, int quantity,
                 LocalDate date, String charityAddress, String donorAddress,
                 String charityPhone, String donorPhone, String donorUsername) {
        this.id = id;
        this.charityUsername = charityUsername;
        this.itemName = itemName;
        this.quantity = quantity;
        this.date = date;
        this.charityAddress = charityAddress;
        this.donorAddress = donorAddress;
        this.charityPhone = charityPhone;
        this.donorPhone = donorPhone;
        // Ensure donorUsername is never null
        this.donorUsername = donorUsername != null ? donorUsername : "Unknown";
        // Set default order status
        this.status = "Pending";
    }
    public String getDeliveryUserName() {
        return this.deliveryUserName; 
    }

    // ===== Getters & Setters =====
    
    // Get the unique order ID
    public String getId() { return id; }

    // Get the name of the ordered item
    public String getItemName() { return itemName; }

    // Get the quantity ordered
    public int getQuantity() { return quantity; }

    // Get the order date
    public LocalDate getDate() { return date; }

    // Get charity address
    public String getCharityAddress() { return charityAddress; }

    // Get donor address
    public String getDonorAddress() { return donorAddress; }
    // Set donor address
    public void setDonorAddress(String donorAddress) { this.donorAddress = donorAddress; }

    // Get charity phone
    public String getCharityPhone() { return charityPhone; }

    // Get donor phone
    public String getDonorPhone() { return donorPhone; }
    // Set donor phone
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    // Get donor username
    public String getDonorUsername() { return donorUsername; }
    // Set donor username
    public void setDonorUsername(String donorUsername) { this.donorUsername = donorUsername; }

    // Get food type
    public FoodType getType() { return type; }
    // Set food type
    public void setType(FoodType type) { this.type = type; }

    // Get order status
    public String getStatus() { return status; }
    // Set order status
    public void setStatus(String status) { this.status = status; }

    // Get charity username
    public String getCharityUsername() { return charityUsername; }
    // Set charity username
    public void setCharityUsername(String charityUsername) { this.charityUsername = charityUsername; }
	public void setDeliveryUsername(String deliveryUserName) {
		 { this.deliveryUserName = deliveryUserName; }
	}
}
