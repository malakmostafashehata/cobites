package backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Charity extends User {
    private List<Order> orders = new ArrayList<>();

    public Charity(String userName, String password, String name, String address,String phone ) {
        super(userName, password, name, address, Role.CHARITY, phone);
    }

    public void addOrder(Order order, NotificationManager nm) {
        orders.add(order);
        if(nm != null){
            nm.notifyCharity(getName(), "Order " + order.getItemName() + " has been created.");
            nm.notifyAdmin("New order " + order.getItemName() + " created by charity " + getName() + ".");
        }
    }

    public List<Order> getOrders() { 
        return Collections.unmodifiableList(orders); 
    }

    @Override
    protected List<Donation> getDonations() {
        return Collections.emptyList();
    }
}
