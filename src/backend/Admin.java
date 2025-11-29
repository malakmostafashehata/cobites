package backend;

import java.util.Collections;
import java.util.List;

public class Admin extends User {

    public Admin(String username, String password, String name,String phone) {
        super(username, password, name, "HQ", Role.ADMIN,phone );
    }

    public void viewAllUsers(UserManager um) { System.out.println(um.getAllUsers()); }
    public void viewAllOrders(OrderManager om) { System.out.println(om.getOrders()); }
    public void viewAllComplaints(ComplaintManager cm) { System.out.println(cm.getComplaints()); }

    public void notify(String message, NotificationManager nm){
        nm.notifyAdmin(message);
    }

    @Override
    protected List<Donation> getDonations() {
        return Collections.emptyList(); // Admin ليس له تبرعات
    }
}