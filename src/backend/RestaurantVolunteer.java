package backend;

public class RestaurantVolunteer extends Volunteer {
    private String restaurantName;

    public RestaurantVolunteer(String userName, String password, String name, String address, String restaurantName, String phone) {
        super(userName, password, name, address, Role.VOLUNTEER, phone);
        this.restaurantName = restaurantName;
    }

    @Override
    public String getVolunteerType() { return "Restaurant"; }

    public String getRestaurantName() { return restaurantName; }
}
