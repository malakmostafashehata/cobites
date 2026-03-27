package backend;

// DeliveryPerson class extends from User class
public class DeliveryPerson extends User {

    // Constructor to create a DeliveryPerson object
    public DeliveryPerson(String userName, String password, String name, String address, String phone) {
        super(userName, password, name, address, Role.DELIVERY, phone);
        this.phone = phone;
    }
    
}