package backend;

import java.time.LocalDate;

public class Complaint {

    private String id;
    private String username;
    private String role;
    private String description;
    private LocalDate date;
    private String status; 

    public Complaint(String id, String role, String username, String description, LocalDate date, String status) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.description = description;
        this.date = date != null ? date : LocalDate.now();
        this.status = status != null ? status : "Pending";
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public String getStatus() { return status; }

    // Setter for status
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return id + "|" + role + "|" + username + "|" + description + "|" + date + "|" + status;
    }
}
