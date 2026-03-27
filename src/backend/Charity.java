package backend;

public class Charity extends User {

    private String code;       
    private String reviewStatus; 

    public Charity(String userName, String password, String name, String address, String phone, String code) {
        super(userName, password, name, address, Role.CHARITY, phone);
        this.code = code;
        this.reviewStatus = "append";
    }

    // ===== Getter & Setter =====

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    @Override
    public String getUserName() {
        return userName;
    }


    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
