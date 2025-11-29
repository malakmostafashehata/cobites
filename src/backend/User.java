package backend;

import java.util.List;

public abstract class User {
    protected String userName, password;
    public String name;
    public String address;
    protected Role role;

    public User(String userName, String password, String name, String address, Role role) {
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.address = address;
        this.role = role;
    }

    // Getters
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public Role getRole() { return role; }

    // Setters لإمكانية تعديل البيانات
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }

    public boolean logIn(String username, String password) {
        return this.userName.equals(username) && this.password.equals(password);
    }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }

	protected abstract List<Donation> getDonations();
}
