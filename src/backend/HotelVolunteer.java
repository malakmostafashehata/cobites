package backend;

public class HotelVolunteer extends Volunteer {
    private String hotelName;

    public HotelVolunteer(String userName, String password, String name, String address, String hotelName, String phone) {
        super(userName, password, name, address, Role.VOLUNTEER, phone);
        this.hotelName = hotelName;
    }

    @Override
    public String getVolunteerType() { return "Hotel"; }

    public String getHotelName() { return hotelName; }
}
