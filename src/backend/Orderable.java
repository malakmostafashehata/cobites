package backend;

// Interface representing objects that can be ordered
// Any class implementing this interface must provide logic for ordering and reducing quantity
public interface Orderable {

    // Check if the requested quantity can be ordered
    // Returns true if the quantity is available, false otherwise
    boolean canOrder(int quantity);  

    // Reduce the available quantity by the specified amount
    // Should be called after a successful order
    void reduceQuantity(int quantity);
}
