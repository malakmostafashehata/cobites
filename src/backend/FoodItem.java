package backend;

import java.time.LocalDate;

// Represents a donated food item
public class FoodItem {
    private String id;                // Unique identifier for the food item
    private String name;              // Name of the food item
    private int qty;                  // Quantity of the food item
    private String volunteerName;     // Name of the volunteer who handled the donation
    private FoodType type;            // Type/category of the food item
    private String imagePath;         // Path to the image of the food item
    private LocalDate donationDate;   // Date when the food item was donated
    private String donorAddress;      // Address of the donor
    private String donorPhone;        // Phone number of the donor

    // ===== Constructor =====
    // Initializes a new FoodItem with all necessary details
    public FoodItem(String id, String name, int qty, String volunteerName,
                    FoodType type, String imagePath, LocalDate donationDate) {
        this.id = id;
        this.name = name;
        this.qty = qty;
        this.volunteerName = volunteerName;
        this.type = type;
        this.imagePath = imagePath;
        this.donationDate = donationDate;
    }

    // ===== Getters & Setters =====

    // Get and set donor address
    public String getDonorAddress() { return donorAddress; }
    public void setDonorAddress(String donorAddress) { this.donorAddress = donorAddress; }

    // Get and set donor phone
    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    // Get and set ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Get and set name
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Get and set quantity
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    // Get volunteer name
    public String getVolunteerName() { return volunteerName; }

    // Get and set type
    public FoodType getType() { return type; }
    public void setType(FoodType type) { this.type = type; }

    // Get image path
    public String getImagePath() { return imagePath; }

    // Get donation date
    public LocalDate getDonationDate() { return donationDate; }
}
