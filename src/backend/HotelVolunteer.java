package backend;

// HotelVolunteer class extends the Volunteer class
// Represents a volunteer who is associated with a hotel
public class HotelVolunteer extends Volunteer {
    private String hotelName;  // Name of the hotel the volunteer represents

    // Constructor for creating a HotelVolunteer
    // Takes username, password, name, address, hotel name, and phone number
    public HotelVolunteer(String userName, String password, String name, String address, String hotelName, String phone) {
        // Call the superclass (Volunteer) constructor
        // Pass Role.VOLUNTEER as the role for this type of volunteer
        super(userName, password, name, address, Role.VOLUNTEER, phone);
        this.hotelName = hotelName;  // Set the hotel name for this volunteer
    }

    // Override the getVolunteerType method to return "Hotel"
    @Override
    public String getVolunteerType() { 
        return "Hotel"; 
    }

    // Getter for the hotel name
    public String getHotelName() { 
        return hotelName; 
    }
}
