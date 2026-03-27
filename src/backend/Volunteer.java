package backend;

import java.util.ArrayList;
import java.util.List;

public abstract class Volunteer extends User {

    // List of donations made by this volunteer
    private List<Donation> donations = new ArrayList<>();

    // Constructor
    public Volunteer(String userName, String password, String name,
                     String address, Role role, String phone) {
        super(userName, password, name, address, role, phone);
    }


    // Get volunteer phone number
    public String getPhone() {
        return phone;
    }

    // Update volunteer phone number
    public void setPhone(String phone) {
        this.phone = phone;
    }
    // Add a new donation to the volunteer donations list
    // Prevents duplicate donations (same donation ID)
    public void addDonation(Donation donation) {
        if (donation == null) return;

        boolean exists = donations.stream()
                .anyMatch(d -> d.getId().equals(donation.getId()));
        if (!exists) {
            donations.add(donation);
        }
    }

    // Replace donations list (used when loading from file)
    public void setDonations(List<Donation> donations) {
        this.donations = (donations != null) ? donations : new ArrayList<>();
    }

    // Each volunteer subclass must define its type
    public abstract String getVolunteerType();
}
