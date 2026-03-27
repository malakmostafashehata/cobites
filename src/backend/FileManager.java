package backend;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

import javafx.collections.ObservableList;

public class FileManager {

	   private static final String USERS_FILE = "users.txt"; // File for storing all users
	    private static final String CHARITY_STACK_FILE = "charity_stack.txt"; // File for charity stock
	    private static final String DONATIONS_FILE = "donations.txt"; // File for all donations

    public static void saveUsers(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                if (u instanceof PersonVolunteer pv) {
                    pw.println("PersonVolunteer|" + pv.getUserName() + "|" + pv.getPassword() + "|" +
                               pv.getName() + "|" + pv.getAddress() + "|" + pv.getPhone());
                } else if (u instanceof RestaurantVolunteer rv) {
                    pw.println("RestaurantVolunteer|" + rv.getUserName() + "|" + rv.getPassword() + "|" +
                               rv.getName() + "|" + rv.getAddress() + "|" + rv.getRestaurantName() + "|" + rv.getPhone());
                } else if (u instanceof HotelVolunteer hv) {
                    pw.println("HotelVolunteer|" + hv.getUserName() + "|" + hv.getPassword() + "|" +
                               hv.getName() + "|" + hv.getAddress() + "|" + hv.getHotelName() + "|" + hv.getPhone());
                } else if (u instanceof Charity c) {
                    pw.println("Charity|" + c.getUserName() + "|" + c.getPassword() + "|" +
                               c.getName() + "|" + c.getAddress() + "|" + c.getPhone() + "|" +
                               c.getCode() + "|" + c.getReviewStatus());
                } else if (u instanceof DeliveryPerson d) {
                    pw.println("Delivery|" + d.getUserName() + "|" + d.getPassword() + "|" +
                               d.getName() + "|" + d.getAddress() + "|" + d.getPhone());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public static Map<String, User> loadUsers() {
        Map<String, User> userMap = new HashMap<>();
        File file = new File(USERS_FILE);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length < 3) continue;

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
                            String address = parts.length >= 5 ? parts[4] : "";
                            String phone = parts.length >= 6 ? parts[5] : "";
                            String code = parts.length >= 7 ? parts[6] : "";
                            String reviewStatus = parts.length >= 8 ? parts[7] : "append";
                            Charity c = new Charity(parts[1], parts[2], parts[3], address, phone, code);
                            c.setReviewStatus(reviewStatus);
                            userMap.put(parts[1], c);
                        }
                        case "Delivery" -> {
                            String address = parts.length >= 5 ? parts[4] : "";
                            String phone = parts.length >= 6 ? parts[5] : "";
                            DeliveryPerson d = new DeliveryPerson(parts[1], parts[2], parts[3], address, phone);
                            userMap.put(parts[1], d);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading users: " + e.getMessage());
            }
        }

        // إضافة حساب admin تلقائياً
        userMap.put("admin", new Admin("Admin", "1234"));

        return userMap;
    }

 
 // ================== Save Donation ==================
    public static void saveDonation(Donation d) {
        if (d == null || d.getItem() == null) return;

        ensureCharityStockFileExists();

        try (PrintWriter pw = new PrintWriter(new FileWriter(DONATIONS_FILE, true))) {
            FoodItem item = d.getItem();
            String donorPhone = (d.getDonorPhone() != null && !d.getDonorPhone().isBlank()) ? d.getDonorPhone() : "N/A";
            String username = d.getDonor() != null ? d.getDonor().getUserName() : "Unknown";

            pw.println(
                d.getId() + "|" +
                username + "|" +
                item.getName() + "|" +
                item.getQty() + "|" +
                d.getDate() + "|" +
                (item.getDonorAddress() != null ? item.getDonorAddress() : "") + "|" +
                donorPhone + "|" +
                item.getType() + "|" +
                (item.getImagePath() != null ? item.getImagePath() : "")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(CHARITY_STACK_FILE, true))) {
            FoodItem item = d.getItem();
            String donorPhone = (d.getDonorPhone() != null && !d.getDonorPhone().isBlank()) ? d.getDonorPhone() : "N/A";
            String username = d.getDonor() != null ? d.getDonor().getUserName() : "Unknown";

            pw.println(
                d.getId() + "|" +
                username + "|" +
                item.getName() + "|" +
                item.getQty() + "|" +
                d.getDate() + "|" +
                (item.getDonorAddress() != null ? item.getDonorAddress() : "") + "|" +
                donorPhone + "|" +
                item.getType() + "|" +
                (item.getImagePath() != null ? item.getImagePath() : "")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static List<Donation> loadDonations(Map<String, User> users, Volunteer currentVolunteer) {
        List<Donation> donations = new ArrayList<>();
        File file = new File(DONATIONS_FILE);
        if (!file.exists()) return donations;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length < 9) continue;

                String orderId = p[0];
                String username = p[1];
                String name = p[2];
                int qty;
                try { qty = Integer.parseInt(p[3]); } catch (Exception e) { qty = 0; }
                LocalDate date = LocalDate.parse(p[4]);

                String donorAddress = p[5];
                String donorPhone = p[6].isBlank() ? "N/A" : p[6];
                FoodType type = FoodType.valueOf(p[7].toUpperCase());
                String imagePath = p.length >= 9 && !p[8].isBlank() ? p[8] : null;

                Volunteer donor = null;
                if (users.containsKey(username) && users.get(username) instanceof Volunteer v) {
                    donor = v;
                } else if (currentVolunteer != null && username.equals(currentVolunteer.getUserName())) {
                    donor = currentVolunteer;
                }

                FoodItem item = new FoodItem(orderId, name, qty, donor != null ? donor.getUserName() : username, type, imagePath, date);
                item.setDonorAddress(donorAddress);
                item.setDonorPhone(donorPhone);

                Donation donation = new Donation(orderId, item, donor, null, date);
                donation.setDonorPhone(donorPhone);

                donations.add(donation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return donations;
    }


    // ================== Update donation ==================
    public static void updateDonation(Donation donation) {
        File file = new File(DONATIONS_FILE);
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts[0].equals(donation.getId())) {
                    FoodItem item = donation.getItem();
                    line = String.join("|",
                    	    donation.getId(),
                    	    item.getName(),
                    	    String.valueOf(item.getQty()),
                    	    donation.getDate().toString(),
                    	    donation.getCharity() != null ? donation.getCharity().getAddress() : "",
                    	    item.getDonorAddress() != null ? item.getDonorAddress() : "",
                    	    donation.getCharityPhone(),
                    	    donation.getDonorPhone(),
                    	    item.getType().name(),
                    	    item.getImagePath() != null ? item.getImagePath() : ""
                    	);

                }
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
            for (String l : lines) pw.println(l);
        } catch (Exception e) {
            e.printStackTrace();}
        }
    public static int getQuantityFromStock(String donationId) {
        File file = new File("charity_stack.txt");
        if (!file.exists()) return 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 4) continue;

                if (parts[0].trim().equals(donationId.trim())) {
                    return Integer.parseInt(parts[3]); 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void addOrIncreaseStock(String id, String name, String donor, int qty) {
        File file = new File(CHARITY_STACK_FILE);
        Map<String, Integer> stockMap = new LinkedHashMap<>();

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length < 4) continue;
                    String key = parts[0].trim(); 
                    int existingQty = Integer.parseInt(parts[3].trim());
                    stockMap.put(key, existingQty);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        stockMap.put(id, stockMap.getOrDefault(id, 0) + qty);

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (Map.Entry<String, Integer> entry : stockMap.entrySet()) {
                pw.println(entry.getKey() + "|" + name + "|" + donor + "|" + entry.getValue());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

     // ================== Charity Stock ==================
    public static void ensureCharityStockFileExists() {
        try {
            File file = new File(CHARITY_STACK_FILE);
            if (!file.exists()) file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateOrdersAfterProfileEdit(User updatedUser) {
        try {
            List<Order> orders = loadOrders(null);
            boolean changed = false;

            for (Order o : orders) {
                if (o.getDonorUsername().equals(updatedUser.getUserName())) {

                    o.setDonorUsername(updatedUser.getUserName());

                    o.setDonorPhone(updatedUser.getPhone());

                    o.setDonorAddress(updatedUser.getAddress());

                    changed = true;
                }
            }

            if (changed) {
                rewriteOrders(orders);
            }

        } catch (Exception e) {
            System.out.println("Error while updating orders: " + e.getMessage());
        }
    }

    public static void updateCharityStack(List<Donation> allDonations) {
        ensureCharityStockFileExists();
        try (PrintWriter pw = new PrintWriter(new FileWriter(CHARITY_STACK_FILE, false))) {
            for (Donation d : allDonations) {
                if (d == null || d.getItem() == null) continue;

                String donorPhone = (d.getDonorPhone() != null && !d.getDonorPhone().isBlank()) ? d.getDonorPhone() : "N/A";

                pw.println(
                	    d.getId() + "|" +
                	    (d.getDonor() != null ? d.getDonor().getUserName() : "Unknown") + "|" +
                	    d.getItem().getName() + "|" +
                	    d.getItem().getQty() + "|" +
                	    d.getDate() + "|" +                  
                	    (d.getItem().getDonorAddress() != null ? d.getItem().getDonorAddress() : "") + "|" +
                	    d.getDonorPhone() + "|" +
                	    d.getItem().getType().name() + "|" +
                	    (d.getItem().getImagePath() != null ? d.getItem().getImagePath() : "")
                	);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static List<Donation> loadCharityStack(Map<String, User> users) {
        ensureCharityStockFileExists();
        List<Donation> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(CHARITY_STACK_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 8) continue; 

                String donationId = parts[0];
                String volunteerName = parts[1];
                String itemName = parts[2];
                int qty = Integer.parseInt(parts[3]);
                LocalDate date = LocalDate.parse(parts[4]);
                String donorAddress = parts[5];
                String donorPhone   = parts[6];
                FoodType type       = FoodType.valueOf(parts[7]);
                String imagePath    = parts.length >= 9 && !parts[8].isBlank() ? parts[8] : null;

                User u = users != null ? users.get(volunteerName) : null;
                Volunteer volunteer = (u instanceof Volunteer v) ? v : null;

                FoodItem item = new FoodItem(donationId, itemName, qty, volunteerName, type, imagePath, date);
                item.setDonorAddress(donorAddress);
                item.setDonorPhone(donorPhone);

                Donation donation = new Donation(donationId, item, volunteer, null, date);
                donation.setDonorPhone(donorPhone);

                list.add(donation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }



 // ================== Orders ==================
  
    private static final String ORDERS_FILE = "orders.txt";

    public static void saveOrder(Order o) {
        if (o == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(ORDERS_FILE, true))) {

            String charityPhone = o.getCharityPhone() != null && !o.getCharityPhone().isBlank() ? o.getCharityPhone() : "N/A";
            String donorPhone   = o.getDonorPhone() != null && !o.getDonorPhone().isBlank() ? o.getDonorPhone() : "N/A";
            String donorUsername = o.getDonorUsername() != null && !o.getDonorUsername().isBlank() ? o.getDonorUsername() : "Unknown"; // هنا نستخدم الـ username بدل الاسم

            String deliveryUsername = o.getDeliveryUserName() != null ? o.getDeliveryUserName() : "Unknown";

            pw.println(
                o.getId() + "|" +
                o.getCharityUsername() + "|" +   
                o.getItemName() + "|" +
                o.getQuantity() + "|" +
                o.getDate() + "|" +
                o.getCharityAddress() + "|" +
                o.getDonorAddress() + "|" +
                charityPhone + "|" +
                donorPhone + "|" +
                donorUsername + "|" +           
                o.getStatus() + "|" +           
                deliveryUsername                
            );

        } catch (Exception e) {
            System.err.println("Error saving order: " + e.getMessage());
        }
    }



 // ================== Orders ==================
    public static List<Order> loadOrders(Map<String, User> users) {
        List<Order> orders = new ArrayList<>();
        File file = new File(ORDERS_FILE);
        if (!file.exists()) return orders;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 12) continue; 

                String orderId         = parts[0];
                String charityUsername = parts[1];
                String itemName        = parts[2];
                int qty                = Integer.parseInt(parts[3]);
                LocalDate date         = LocalDate.parse(parts[4]);
                String charityAddress  = parts[5];
                String donorAddress    = parts[6];
                String charityPhone    = parts[7];
                String donorPhone      = parts[8];
                String donorUsername   = parts[9];  
                String status          = parts[10];
                String deliveryUsername= parts[11]; 

                Order o = new Order(orderId, charityUsername, itemName, qty, date,
                                    charityAddress, donorAddress, charityPhone, donorPhone, donorUsername);
                o.setStatus(status);
                o.setDeliveryUsername(deliveryUsername);

                orders.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }




    public static void rewriteOrders(List<Order> orders) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ORDERS_FILE,false))) {
            for (Order o : orders) {
                String charityPhone = o.getCharityPhone() != null && !o.getCharityPhone().isBlank() ? o.getCharityPhone() : "N/A";
                String donorPhone = o.getDonorPhone() != null && !o.getDonorPhone().isBlank() ? o.getDonorPhone() : "N/A";
                String charityUsername = o.getCharityUsername() != null ? o.getCharityUsername() : "Unknown";

                pw.println(
                    o.getId() + "|" +
                    charityUsername + "|" +
                    o.getItemName() + "|" +
                    o.getQuantity() + "|" +
                    o.getDate() + "|" +
                    o.getCharityAddress() + "|" +
                    o.getDonorAddress() + "|" +
                    charityPhone + "|" +
                    donorPhone + "|" +
                    o.getDonorUsername()+ "|" +
                    o.getStatus()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void updateOrderStatus(String orderId, String newStatus, String completedBy) {
        File file = new File(ORDERS_FILE);
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);

                if (parts[0].equals(orderId)) {
                    if (parts.length < 12) {
                        String[] newParts = new String[12];
                        for (int i = 0; i < 12; i++) {
                            newParts[i] = i < parts.length ? parts[i] : "";
                        }
                        parts = newParts;
                    }

                    parts[10] = newStatus; 
                    parts[11] = completedBy; 
                    line = String.join("|", parts);
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





 // ================== Complaints ==================
    private static final String COMPLAINTS_FILE = "complaints.txt";
    public static void saveComplaint(Complaint c) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(COMPLAINTS_FILE, true))) {
            pw.println(
                c.getId() + "|" +
                c.getRole() + "|" +
                c.getUsername() + "|" +
                c.getDescription() + "|" +
                c.getDate() + "|" +
                c.getStatus() 
            );
        } catch (Exception e) {
            System.err.println("Error saving complaint: " + e.getMessage());
        }
    }
    public static void saveAllComplaints(ObservableList<List<String>> complaintsData) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(COMPLAINTS_FILE, false))) {
            for (List<String> row : complaintsData) {
                String line = String.join("|", 
                        row.get(0), // ID
                        row.get(4), // Role
                        row.get(1), // Username
                        row.get(2), // Description
                        row.get(3), // Date
                        row.get(5)  // Status
                );
                pw.println(line);
            }
        } catch (Exception e) {
            System.err.println("Error saving all complaints: " + e.getMessage());
        }
    }

    public static List<Complaint> loadComplaints() {
        List<Complaint> complaints = new ArrayList<>();
        File file = new File(COMPLAINTS_FILE);
        if (!file.exists()) return complaints;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 6) continue; 
                complaints.add(new Complaint(parts[0], parts[1], parts[2], parts[3], LocalDate.parse(parts[4]), parts[5]));
            }
        } catch (Exception e) {
            System.err.println("Error loading complaints: " + e.getMessage());
        }
        return complaints;
    }


    public static void rewriteDonations(List<Donation> donations) {
        if (donations == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(DONATIONS_FILE, false))) { 
            for (Donation d : donations) {
                if (d == null || d.getItem() == null) continue;

                FoodItem item = d.getItem();

                String id = d.getId() != null ? d.getId() : "";
                String username = d.getDonor() != null ? d.getDonor().getUserName() : "";
                String name = item.getName() != null ? item.getName() : "";
                String qty = String.valueOf(item.getQty());
                String date = d.getDate() != null ? d.getDate().toString() : "";
                String donorAddress = item.getDonorAddress() != null ? item.getDonorAddress() : "";
                String donorPhone = item.getDonorPhone() != null ? item.getDonorPhone() : "";
                String type = item.getType() != null ? item.getType().name() : "";
                String imagePath = item.getImagePath() != null ? item.getImagePath() : "";

                pw.println(String.join("|",
                    id,
                    username,      
                    name,
                    qty,
                    date,
                    donorAddress,
                    donorPhone,
                    type,
                    imagePath
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void updateCharityInfo(Charity charity, List<Order> orders, List<Donation> donations) {
        // ===== تحديث الأوردرات =====
        File ordersFile = new File(ORDERS_FILE);
        List<String> orderLines = new ArrayList<>();
        if (ordersFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(ordersFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 2 && parts[1].equals(charity.getUserName())) {
                        parts[5] = charity.getAddress(); 
                        parts[7] = charity.getPhone();                       }
                    orderLines.add(String.join("|", parts));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(ordersFile, false))) {
                for (String l : orderLines) pw.println(l);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (donations != null) {
            for (Donation d : donations) {
                if (d.getCharity() != null && d.getCharity().getUserName().equals(charity.getUserName())) {
                    d.getItem().setDonorAddress(charity.getAddress());
                    d.setCharityPhone(charity.getPhone());
                }
            }
            rewriteDonations(donations); 
        }
    }


    public static void removeOrder(Order orderToRemove) {
        File ordersFile = new File(ORDERS_FILE);
        File stockFile = new File(CHARITY_STACK_FILE);

        List<String> ordersLines = new ArrayList<>();
        List<String> stockLines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ordersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 1 && !parts[0].equals(orderToRemove.getId())) {
                    ordersLines.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(ordersFile))) {
            for (String l : ordersLines) pw.println(l);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ensureCharityStockFileExists();

        try (BufferedReader br = new BufferedReader(new FileReader(stockFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                stockLines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean found = false;
        for (int i = 0; i < stockLines.size(); i++) {
            String[] parts = stockLines.get(i).split("\\|", -1);

            if (parts.length >= 4 && parts[2].equals(orderToRemove.getItemName())) {
                int currentQty = Integer.parseInt(parts[3]);
                parts[3] = String.valueOf(currentQty + orderToRemove.getQuantity());
                stockLines.set(i, String.join("|", parts));
                found = true;
                break;
            }
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(stockFile))) {
            for (String l : stockLines) pw.println(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void increaseStockFromOrder(Order o) {
        File stockFile = new File(CHARITY_STACK_FILE);
        List<String> stockLines = new ArrayList<>();

        if (!stockFile.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(stockFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                stockLines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        boolean found = false;

        for (int i = 0; i < stockLines.size(); i++) {
            String[] parts = stockLines.get(i).split("\\|", -1);

            if (parts.length >= 4 && parts[2].equals(o.getItemName())) {
                int currentQty = Integer.parseInt(parts[3]);
                parts[3] = String.valueOf(currentQty + o.getQuantity());
                stockLines.set(i, String.join("|", parts));
                found = true;
                break;
            }
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(stockFile))) {
            for (String l : stockLines) pw.println(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static List<Order> loadOrdersByDeliveryUsername(String deliveryUsername) {
        List<Order> orders = new ArrayList<>();
        File file = new File(ORDERS_FILE);
        if (!file.exists()) return orders;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length < 11) continue;

                String orderId         = parts[0];
                String deliveryUser    = parts[1]; 
                String itemName        = parts[2];
                int qty                = Integer.parseInt(parts[3]);
                LocalDate date         = LocalDate.parse(parts[4]);
                String charityAddress  = parts[5];
                String donorAddress    = parts[6];
                String charityPhone    = parts[7];
                String donorPhone      = parts[8];
                String donorUsername   = parts[9];
                String status          = parts[10];

                if (!deliveryUser.equals(deliveryUsername)) continue;

                Order o = new Order(orderId, deliveryUser, itemName, qty, date,
                                    charityAddress, donorAddress, charityPhone, donorPhone, donorUsername);
                o.setStatus(status);

                orders.add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }


}