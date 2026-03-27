package backend;

public abstract class User {


    // Unique username used for login
    protected String userName;

    // User password
    protected String password;

    // Full name of the user
    protected String name;

    // User address
    protected String address;

    // Role of the user (ADMIN, CHARITY, VOLUNTEER, DELIVERY, etc.)
    protected Role role;

    // User phone number
    protected String phone;

    // ================= Constructor =================

    public User(String userName, String password, String name,
                String address, Role role, String phone) {
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.address = address;
        this.role = role;
        this.phone = phone;
    }

    // ================= Getters =================

    // Get username
    public String getUserName() {
        return userName;
    }

    // Get password
    public String getPassword() {
        return password;
    }

    // Get full name
    public String getName() {
        return name;
    }

    // Get user address
    public String getAddress() {
        return address;
    }

    // Get user role
    public Role getRole() {
        return role;
    }

    // Get phone number
    public String getPhone() {
        return phone;
    }

    // ================= Setters =================

    // Update username
    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Update password
    public void setPassword(String password) {
        this.password = password;
    }

    // Update full name
    public void setName(String name) {
        this.name = name;
    }

    // Update user address
    public void setAddress(String address) {
        this.address = address;
    }

    // Update phone number
    public void setPhone(String phone) {
        this.phone = phone;
    }


    // String representation of the user
    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
