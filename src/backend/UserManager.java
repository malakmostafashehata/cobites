package backend;

import java.util.*;

public class UserManager {

    private static UserManager instance;
    private Map<String, User> users = new HashMap<>();

    private UserManager() {
        users.putAll(FileManager.loadUsers());
    }
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    public void saveNow() {
        FileManager.saveUsers(getAllUsers());
    }

    public static UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    public void addUser(User u) {
        users.put(u.getUserName(), u);
        save();
    }

    public void deleteUser(User u) {
        users.remove(u.getUserName());
        save();
    }

    public boolean usernameExists(String username) {
        return users.containsKey(username);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> checkLogin(String username, String password) {

        if (username == null || password == null) {
            return Optional.empty();
        }

        String inputUsername = username.trim();
        String inputPassword = password.trim();

        return users.values().stream()
                .filter(u ->
                        u.getUserName().equals(inputUsername) &&   
                                u.getPassword().equals(inputPassword)
                )
                .findFirst();
    }

    private void save() {
        FileManager.saveUsers(getAllUsers());
    }
}