package backend;

public class RestaurantVolunteer extends Volunteer {
    private String restaurantName, phone;

    public RestaurantVolunteer(String userName, String password, String name, String address, String restaurantName, String phone) {
        super(userName, password, name, address, Role.VOLUNTEER);
        this.restaurantName = restaurantName;
        this.phone = phone;
    }

    @Override
    public String getVolunteerType() { return "Restaurant"; }

    public String getPhone() { return phone; }

    public String getRestaurantName() { return restaurantName; }
}
