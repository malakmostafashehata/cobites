package backend;

public class PersonVolunteer extends Volunteer {
    private String phone;

    public PersonVolunteer(String userName, String password, String name, String address, String phone) {
        super(userName, password, name, address, Role.VOLUNTEER);
        this.phone = phone;
    }

    @Override
    public String getVolunteerType() { return "Person"; }

    public String getPhone() { return phone; }
}