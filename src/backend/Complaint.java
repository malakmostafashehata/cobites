package backend;

import java.time.LocalDate;

public class Complaint {

    private String id;
    private String charityName;
    private String description;
    private LocalDate date;

    public Complaint(String id, String charityName, String description) {
        this.id = id;
        this.charityName = charityName;
        this.description = description;
        this.date = LocalDate.now();
    }


	public String getId() {
        return id;
    }

    public String getCharityName() {
        return charityName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Complaint ID: " + id +
               " | Charity: " + charityName +
               " | Description: " + description +
               " | Date: " + date;
    }
}
