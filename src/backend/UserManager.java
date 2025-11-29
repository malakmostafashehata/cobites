package backend;

import java.util.*;

public class UserManager {

    private static UserManager instance;
    private List<User> users = new ArrayList<>();

    private UserManager() {
        // Load users
        Map<String, User> loadedUsers = FileManager.loadUsers();
        users.addAll(loadedUsers.values());

        // Load all donations & attach to volunteers
        List<Donation> allDonations = FileManager.loadDonations(loadedUsers);

        for (Donation d : allDonations) {
            User u = loadedUsers.get(d.getDonor().getName());
            if (u instanceof Volunteer vol) {
                vol.addDonation(d);
            }
        }
    }

    public static UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    public void addUser(User u) {
        users.add(u);
        FileManager.saveUsers(users);
    }

    public List<User> getAllUsers() {
        return users;
    }

    public Optional<User> checkLogin(String username, String password) {
        if (username == null || password == null) return Optional.empty();

        String trimmedUsername = username.trim().toLowerCase();
        String trimmedPassword = password.trim();

        return users.stream()
                .filter(u -> u.getUserName().trim().equalsIgnoreCase(trimmedUsername)
                        && u.getPassword().trim().equals(trimmedPassword))
                .findFirst();
    }

    public void deleteUser(Volunteer volunteer) {
        users.remove(volunteer);
        FileManager.saveUsers(users);
    }

    public void deleteUser(Charity charity) {
        users.remove(charity);
        FileManager.saveUsers(users);
    }
}