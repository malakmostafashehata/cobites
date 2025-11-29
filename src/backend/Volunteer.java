package backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Volunteer extends User {
    private List<Donation> donations = new ArrayList<>();
    private String phone;

    public Volunteer(String userName, String password, String name, String address, Role role, String phone) {
        super(userName, password, name, address, role,phone);
        this.phone = phone;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public void addDonation(Donation donation) {
        donations.add(donation);
    }

    public void updateDonation(FoodItem oldItem, Donation updatedDonation) {
        for (int i = 0; i < donations.size(); i++) {
            if (donations.get(i).getItem().getId().equals(oldItem.getId())) {
                donations.set(i, updatedDonation);
                FileManager.saveDonation(updatedDonation);
                break;
            }
        }
    }

    public Donation getDonationByItemId(String itemId) {
        for(Donation d : donations) {
            if(d.getItem().getId().equals(itemId)) return d;
        }
        return null;
    }

    public List<Donation> getDonations() {
        return Collections.unmodifiableList(donations);
    }

    public abstract String getVolunteerType();
}
