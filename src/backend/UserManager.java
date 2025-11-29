package backend;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager {

    private static UserManager instance;

    // Map بدل List لتسهيل التعامل مع username كمفتاح
    private Map<String, User> users = new HashMap<>();

    private UserManager() {
        // Load users from file
        Map<String, User> loadedUsers = FileManager.loadUsers();
        users.putAll(loadedUsers);

        // Load all donations & attach to volunteers
        List<Donation> allDonations = FileManager.loadDonations(loadedUsers);
        for (Donation d : allDonations) {
            User u = loadedUsers.get(d.getDonor().getUserName());
            if (u instanceof Volunteer vol) {
                vol.addDonation(d);
            }
        }
    }

    public static UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    // ================== CRUD ==================

    public void addUser(User u) {
        users.put(u.getUserName(), u);
        saveToFile();
    }

    public void deleteUser(User u) {
        users.remove(u.getUserName());
        saveToFile();
    }

    public boolean usernameExists(String username) {
        return users.containsKey(username);
    }

    public void updateUsername(User u, String newUsername) {
        if (!users.containsKey(u.getUserName())) return;

        users.remove(u.getUserName());
        u.setUserName(newUsername);
        users.put(newUsername, u);
        saveToFile();
    }

    public void updateUser(User u) {
        // حفظ أي تغييرات في ملف
        users.put(u.getUserName(), u);
        saveToFile();
    }

    private void saveToFile() {
        FileManager.saveUsers(new ArrayList<>(users.values()));
    }

    // ================== Getters ==================

    public Map<String, User> getAllUsersMap() {
        return users;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> checkLogin(String username, String password) {
        if (username == null || password == null) return Optional.empty();

        String trimmedUsername = username.trim().toLowerCase();
        String trimmedPassword = password.trim();

        return users.values().stream()
                .filter(u -> u.getUserName().trim().equalsIgnoreCase(trimmedUsername)
                        && u.getPassword().trim().equals(trimmedPassword))
                .findFirst();
    }
}
