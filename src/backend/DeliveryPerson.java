package backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeliveryPerson extends User {

    private String phone; // optional if you want to store number
    private List<Order> assignedOrders = new ArrayList<>();

    public DeliveryPerson(String userName, String password, String name, String address, String phone) {
        super(userName, password, name, address, Role.DELIVERY, phone);
        this.phone = phone;
    }

    public List<Order> getAssignedOrders() {
        return Collections.unmodifiableList(assignedOrders);
    }

    public void assignOrder(Order o) {
        assignedOrders.add(o);
        o.setDeliveryPerson(this);
        // Order goes directly to delivery screen, no notification
    }

    public void deliverOrder(Order o) {
        assignedOrders.remove(o);
        // Delivery done
    }

    public String getPhone() { return phone; }

	@Override
	public List<Donation> getDonations() {
	    return Collections.emptyList(); // لأنه مش متطوع، ما عندوش تبرعات
	}

}