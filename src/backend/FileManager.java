package backend;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class FileManager {

    private static final String USERS_FILE = "users.txt";
    private static final String DONATIONS_FILE = "donations.txt";
    private static NotificationManager notificationManager = new NotificationManager();

    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

 // ================== Save Users ==================
    public static void saveUsers(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
            	if (u instanceof PersonVolunteer pv) {
            	    pw.println("PersonVolunteer|" + pv.getUserName() + "|" + pv.getPassword() + "|" +
            	               pv.getName() + "|" + pv.getAddress() + "|" +
            	               (pv.getPhone() != null ? pv.getPhone() : ""));
            	} else if (u instanceof RestaurantVolunteer rv) {
            	    pw.println("RestaurantVolunteer|" + rv.getUserName() + "|" + rv.getPassword() + "|" +
            	               rv.getName() + "|" + rv.getAddress() + "|" + rv.getRestaurantName() + "|" +
            	               (rv.getPhone() != null ? rv.getPhone() : ""));
            	} else if (u instanceof HotelVolunteer hv) {
            	    pw.println("HotelVolunteer|" + hv.getUserName() + "|" + hv.getPassword() + "|" +
            	               hv.getName() + "|" + hv.getAddress() + "|" + hv.getHotelName() + "|" +
            	               (hv.getPhone() != null ? hv.getPhone() : ""));
            	} else if (u instanceof DeliveryPerson d) {
            	    pw.println("Delivery|" + d.getUserName() + "|" + d.getPassword() + "|" +
            	               d.getName() + "|" + d.getAddress() + "|" +
            	               (d.getPhone() != null ? d.getPhone() : ""));
            	
                } else if (u instanceof Charity c) {
                    pw.println("Charity|" + c.getUserName() + "|" + c.getPassword() + "|" +
                            c.getName() + "|" + c.getAddress() + "|" +
                            (c.getPhone() != null ? c.getPhone() : ""));
                } else if (u instanceof DeliveryPerson d) {
                    pw.println("Delivery|" + d.getUserName() + "|" + d.getPassword() + "|" +
                            d.getName() + "|" + d.getAddress() + "|" +
                            (d.getPhone() != null ? d.getPhone() : ""));
                } else if (u instanceof Admin a) {
                    pw.println("Admin|" + a.getUserName() + "|" + a.getPassword() + "|" +
                            a.getName() + "|" + a.getAddress());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    // ================== Load Users ==================
    public static Map<String, User> loadUsers() {
        Map<String, User> userMap = new HashMap<>();
        File file = new File(USERS_FILE);
        if (!file.exists()) return userMap;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                switch (parts[0]) {
                case "PersonVolunteer" -> {
                    String phone = parts.length >= 6 ? parts[5] : "";
                    PersonVolunteer pv = new PersonVolunteer(parts[1], parts[2], parts[3], parts[4], phone);
                    userMap.put(parts[1], pv);
                }

                case "RestaurantVolunteer" -> {
                    String restaurantName = parts[5];
                    String phone = parts.length >= 7 ? parts[6] : "";
                    RestaurantVolunteer rv = new RestaurantVolunteer(parts[1], parts[2], parts[3], parts[4], restaurantName, phone);
                    userMap.put(parts[1], rv);
                }

                case "HotelVolunteer" -> {
                    String hotelName = parts[5];
                    String phone = parts.length >= 7 ? parts[6] : "";
                    HotelVolunteer hv = new HotelVolunteer(parts[1], parts[2], parts[3], parts[4], hotelName, phone);
                    userMap.put(parts[1], hv);
                }

                    case "Charity" -> {
                        String phone = parts.length >= 5 && !parts[4].equals("null") ? parts[4] : "";
                        Charity c = new Charity(parts[1], parts[2], parts[3], parts[4], phone);
                        userMap.put(parts[1], c);
                    }
                    case "Delivery" -> {
                        String phone = parts.length >= 5 && !parts[4].equals("null") ? parts[4] : "";
                        DeliveryPerson d = new DeliveryPerson(parts[1], parts[2], parts[3], parts[4], phone);
                        userMap.put(parts[1], d);
                    }
                    case "Admin" -> {
                        Admin a = new Admin(parts[1], parts[2], parts[3], parts[4]);
                        userMap.put(parts[1], a);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }

        return userMap;
    }


    // ================== Donations ==================
    public static void saveDonation(Donation d) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DONATIONS_FILE, true))) {
            FoodItem item = d.getItem();
            pw.println(d.getId() + "|" + item.getId() + "|" + item.getName() + "|" +
                    item.getQty() + "|" + d.getDonor().getUserName() + "|" +
                    item.getType() + "|" + d.getDate() + "|" + item.getImagePath());
        } catch (Exception e) {
            System.err.println("Error saving donation: " + e.getMessage());
        }
    }

    public static List<Donation> loadDonations(Map<String, User> users) {
        List<Donation> donations = new ArrayList<>();
        File file = new File(DONATIONS_FILE);
        if (!file.exists()) return donations;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length < 8) continue;

                String donationId = p[0];
                String itemId = p[1];
                String name = p[2];
                int qty = Integer.parseInt(p[3]);
                String volunteerUserName = p[4];
                FoodType type = FoodType.valueOf(p[5]);
                LocalDate date = LocalDate.parse(p[6]);
                String imagePath = p[7];

                User u = users.get(volunteerUserName);
                if (!(u instanceof Volunteer volunteer)) continue;

                boolean exists = volunteer.getDonations().stream().anyMatch(d -> d.getId().equals(donationId));
                if (exists) continue;

                FoodItem item = new FoodItem(itemId, name, qty, volunteer.getUserName(), type, imagePath, date);
                Donation d = new Donation(donationId, item, volunteer, date);
                volunteer.addDonation(d);
                donations.add(d);
            }
        } catch (Exception e) {
            System.err.println("Error loading donations: " + e.getMessage());
        }

        return donations;
    }

    // ================== Update donation ==================
    public static void updateDonation(Donation updatedDonation) {
        File file = new File(DONATIONS_FILE);
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts[0].equals(updatedDonation.getId())) {
                    FoodItem item = updatedDonation.getItem();
                    line = updatedDonation.getId() + "|" + item.getId() + "|" + item.getName() + "|" +
                            item.getQty() + "|" + updatedDonation.getDonor().getUserName() + "|" +
                            item.getType() + "|" + updatedDonation.getDate() + "|" + item.getImagePath();
                }
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
            for (String l : lines) pw.println(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 // ================== Orders ==================
    private static final String ORDERS_FILE = "orders.txt";

    public static void saveOrder(Order o) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ORDERS_FILE, true))) {
            pw.println(o.getCharityName() + "|" + o.getItemName() + "|" + o.getQuantity() + "|" + o.getDate());
        } catch (Exception e) {
            System.err.println("Error saving order: " + e.getMessage());
        }
    }

    // لو عايز كمان تقدر تضيف دالة لتحميل الأوردرات
    public static List<Order> loadOrders() {
        List<Order> orders = new ArrayList<>();
        File file = new File(ORDERS_FILE);
        if (!file.exists()) return orders;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                String charityName = parts[0];
                String itemName = parts[1];
                int qty = Integer.parseInt(parts[2]);
                LocalDate date = LocalDate.parse(parts[3]);

                orders.add(new Order(charityName, itemName, qty, date));
            }
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
        return orders;
    }
 // ================== Complaints ==================
    private static final String COMPLAINTS_FILE = "complaints.txt";

    public static void saveComplaint(Complaint c) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(COMPLAINTS_FILE, true))) {
            pw.println(c.getId() + "|" + c.getCharityName() + "|" + c.getDescription());
        } catch (Exception e) {
            System.err.println("Error saving complaint: " + e.getMessage());
        }
    }

    // لو عايز كمان تقدر تضيف دالة لتحميل الشكاوى
    public static List<Complaint> loadComplaints() {
        List<Complaint> complaints = new ArrayList<>();
        File file = new File(COMPLAINTS_FILE);
        if (!file.exists()) return complaints;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 3) continue;

                complaints.add(new Complaint(parts[0], parts[1], parts[2]));
            }
        } catch (Exception e) {
            System.err.println("Error loading complaints: " + e.getMessage());
        }
        return complaints;
    }


}