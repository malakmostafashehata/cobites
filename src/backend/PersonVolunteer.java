package backend;

// PersonVolunteer class extends the Volunteer class
// Represents a volunteer who is an individual person
public class PersonVolunteer extends Volunteer {

    // Constructor for creating a PersonVolunteer
    public PersonVolunteer(String userName, String password, String name, String address, String phone) {
        // Call the superclass (Volunteer) constructor
        // Pass Role.VOLUNTEER as the role for this type of volunteer
        super(userName, password, name, address, Role.VOLUNTEER, phone);
    }

    // Override the getVolunteerType method to return "Person"
    @Override
    public String getVolunteerType() { 
        return "Person"; 
    }
}
