package backend;
//RestaurantVolunteer class extends the Volunteer class

public class RestaurantVolunteer extends Volunteer {

    // Name of the restaurant associated with the volunteer
    private String restaurantName;

    // Constructor for restaurant volunteer
    public RestaurantVolunteer(String userName,
                               String password,
                               String name,
                               String address,
                               String restaurantName,
                               String phone) {

        // Call parent (Volunteer) constructor
        super(userName, password, name, address, Role.VOLUNTEER, phone);

        // Set restaurant name
        this.restaurantName = restaurantName;
    }

    // Return the type of volunteer
    @Override
    public String getVolunteerType() {
        return "Restaurant";
    }

    // Get restaurant name
    public String getRestaurantName() {
        return restaurantName;
    }
}
