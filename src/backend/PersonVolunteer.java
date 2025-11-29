package backend;

public class PersonVolunteer extends Volunteer {

    public PersonVolunteer(String userName, String password, String name, String address, String phone) {
        super(userName, password, name, address, Role.VOLUNTEER, phone);
    }

    @Override
    public String getVolunteerType() { return "Person"; }
}
