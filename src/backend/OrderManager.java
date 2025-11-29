package backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderManager {
    private List<Order> orders = new ArrayList<>();
    private List<DeliveryPerson> deliveryPool = new ArrayList<>();

    public OrderManager(NotificationManager nm){ }

    public void registerDeliveryPerson(DeliveryPerson d){ deliveryPool.add(d); }

    public void createOrder(Order o) {
        orders.add(o);
        if(!deliveryPool.isEmpty()){
            DeliveryPerson d = deliveryPool.get(0);
            d.assignOrder(o);
        }
    }

    public List<Order> getOrders() { return Collections.unmodifiableList(orders); }

    // تحويل Donations → Orders
    public void createOrdersFromDonations(List<Donation> donations) {
        for (Donation d : donations) {
            Order o = new Order(
                d.getItem().getName(),
                d.getItem().getType().toString(),
                d.getItem().getQty(), null
            );
            createOrder(o);
        }
    }
}
