package backend;


// Admin class extends from User class
public class Admin extends User {

    // Constructor to create an Admin object
    public Admin(String username, String password) {
        super(username, password, "", "", Role.ADMIN, "");
    }

}