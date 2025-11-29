package backend;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationManager {

    private List<Notification> notifications = new ArrayList<>();

    public void add(String message){ notifications.add(new Notification(message)); }

    public void notifyCharity(String charityName, String message){ add("Charity " + charityName + ": " + message); }

    public void notifyAdmin(String message){ add("Admin: " + message); }

    public void notifyVolunteer(String volunteerName, String message){ add("Volunteer " + volunteerName + ": " + message); }

    public List<String> getAll(){
        return notifications.stream().map(Notification::toString).collect(Collectors.toList());
    }

    public List<String> getByType(String type){
        return notifications.stream().filter(n -> n.toString().contains(type)).map(Notification::toString).collect(Collectors.toList());
    }
}
