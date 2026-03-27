package backend;

import java.time.LocalDate;

// Donation class represents a donated food item by a volunteer to a charity
// Implements Orderable to allow quantity checking and reduction
public class Donation implements Orderable {
    private String id;               // Unique ID for the donation
    private FoodItem item;           // The food item being donated
    private Volunteer donor;         // Volunteer who donates the item
    private Charity charity;         // Charity receiving the donation (optional)
    private LocalDate date;          // Date of donation
    private String donorPhone;       // Phone of the donor (can be set manually)
    private String charityPhone;     // Phone of the charity (can be set manually)

    // ===== Constructors =====

    // Full constructor including charity
    public Donation(String id, FoodItem item, Volunteer donor, Charity charity, LocalDate date) {
        this.id = id;
        this.item = item;
        this.donor = donor;
        this.charity = charity;
        this.date = date;
    }

    // Constructor without charity
    public Donation(String id, FoodItem item, Volunteer donor, LocalDate date) {
        this(id, item, donor, null, date);
    }

    // ===== Getters & Setters =====
    public String getId() { return id; }
    public FoodItem getItem() { return item; }
    public Volunteer getDonor() { return donor; }
    public Charity getCharity() { return charity; }
    public LocalDate getDate() { return date; }

    // Get donor address 
    public String getDonorAddress() {
        return donor != null ? donor.getAddress() : null;
    }

    // Get donor phone, fallback to donor object or "N/A"
    public String getDonorPhone() {
        return donorPhone != null ? donorPhone : (donor != null ? donor.getPhone() : "N/A");
    }

    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    // Get charity phone, fallback to charity object or "N/A"
    public String getCharityPhone() {
        return charityPhone != null ? charityPhone : (charity != null ? charity.getPhone() : "N/A");
    }

    public void setCharityPhone(String charityPhone) { this.charityPhone = charityPhone; }

    // ===== Orderable implementation =====

    // Check if the donation has enough quantity for an order
    @Override
    public boolean canOrder(int quantity) {
        return item.getQty() >= quantity;
    }

    // Reduce the quantity of the food item after an order
    @Override
    public void reduceQuantity(int quantity) {
        item.setQty(item.getQty() - quantity);
    }

    // Optional setter for ID (currently empty)
    public void setId(String donationId) {
        // Can be implemented if needed
    }
}
