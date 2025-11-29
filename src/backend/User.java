package backend;

import java.util.List;

public abstract class User {
    protected String userName, password;
    public String name;
    public String address;
    protected Role role;
	public String phone;

    public User(String userName, String password, String name, String address, Role role,String phone ) {
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.address = address;
        this.role = role;
        this.phone = phone;
    }

    // Getters
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Role getRole() { return role; }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Setters لإمكانية تعديل البيانات
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() {
        return phone;
    }
    public boolean logIn(String username, String password) {
        return this.userName.equals(username) && this.password.equals(password);
    }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }

	protected abstract List<Donation> getDonations();
}
