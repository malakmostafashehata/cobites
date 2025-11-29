package backend;

import java.time.LocalDate;

public class Donation implements Orderable {
    private String id;
    private FoodItem item;
    private Volunteer donor;
    private Charity charity;
    private LocalDate date;

    public Donation(String id, FoodItem item, Volunteer donor, Charity charity, LocalDate date) {
        this.id = id;
        this.item = item;
        this.donor = donor;
        this.charity = charity;
        this.date = date;
    }

    public Donation(String id, FoodItem item, Volunteer donor, LocalDate date) {
        this.id = id;
        this.item = item;
        this.donor = donor;
        this.date = date;
    }

    public String getId() { return id; }
    public FoodItem getItem() { return item; }
    public Volunteer getDonor() { return donor; }
    public Charity getCharity() { return charity; }
    public LocalDate getDate() { return date; }

    @Override
    public String toString() {
        return "Donation " + id + " of " + item.getName() + " on " + date +
               (charity != null ? " for " + charity.getName() : "");
    }

    // -------- IMPLEMENTING ORDERABLE --------
    @Override
    public boolean canOrder(int quantity) {
        return item.getQty() >= quantity;
    }

    @Override
    public void reduceQuantity(int quantity) {
        int newQty = item.getQty() - quantity;
        item.setQty(newQty);
    }
}
