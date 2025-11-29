package backend;

import java.time.LocalDate;
import java.util.UUID;

public class Order {
    private String id;
    private String charityName;
    private String itemName;
    private int quantity;
    private LocalDate date;
    private DeliveryPerson deliveryPerson;

    public Order(String charityName, String itemName, int quantity, LocalDate date) {
        this.id = UUID.randomUUID().toString();
        this.charityName = charityName;
        this.itemName = itemName;
        this.quantity = quantity;
        this.date = date != null ? date : LocalDate.now();
    }
    private FoodType type; // جديد

    public FoodType getType() { return type; }

    // عند الإنشاء، عيّن type = item.getType()

    public String getId() { return id; }
    public String getCharityName() { return charityName; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public LocalDate getDate() { return date; }

    public DeliveryPerson getDeliveryPerson() { return deliveryPerson; }
    public void setDeliveryPerson(DeliveryPerson deliveryPerson) { this.deliveryPerson = deliveryPerson; }

    public void setDate(LocalDate date) { this.date = date; }
}
