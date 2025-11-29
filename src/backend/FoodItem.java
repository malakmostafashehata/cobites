package backend;

import java.time.LocalDate;

public class FoodItem {
    private String id;
    private String name;
    private int qty;
    private String volunteerName;
    private FoodType type;
    private String imagePath;
    private LocalDate donationDate;

    // Constructor
    public FoodItem(String id, String name, int qty, String volunteerName, FoodType type, String imagePath,LocalDate donationDate){
        this.id = id;
        this.name = name;
        this.qty = qty;
        this.volunteerName = volunteerName;
        this.type = type;
        this.imagePath = imagePath;
        this.donationDate = donationDate;
    }
    public void setQty(int qty) {
        this.qty = qty;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public int getQty() { return qty; }
    public String getVolunteerName() { return volunteerName; }
    public FoodType getType() { return type; }
    public String getImagePath() { return imagePath; }
    public LocalDate getDonationDate() { return donationDate; }
	public void setImagePath(String imagePath2) {
		
	}
	public void setType(FoodType value) {
		// TODO Auto-generated method stub
		
	}
	public void setName(String name2) {
		// TODO Auto-generated method stub
		
	}
}
