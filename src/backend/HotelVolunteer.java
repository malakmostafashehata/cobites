package backend;

public class HotelVolunteer extends Volunteer {
    private String hotelName, phone;

    public HotelVolunteer(String userName, String password, String name, String address, String hotelName, String phone) {
        super(userName, password, name, address, Role.VOLUNTEER);
        this.hotelName = hotelName;
        this.phone = phone;
    }

    @Override
    public String getVolunteerType() { return "Hotel"; }

    public String getPhone() { return phone; }

    public String getHotelName() { return hotelName; }
}