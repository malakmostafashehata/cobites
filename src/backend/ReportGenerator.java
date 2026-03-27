package backend;

import javafx.collections.ObservableList;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

public class ReportGenerator {

    // ================== EXPORT DONATIONS ==================
    public static void exportDonationsCsv(List<FoodItem> donations, String fileName) {
        try (PrintWriter pw = new PrintWriter(fileName)) {

            // ===== Header Row =====
            pw.println("Name,Type,Quantity,Volunteer");

            // ===== Data Rows =====
            for (FoodItem it : donations) {
                pw.println(
                        it.getName() + "," +
                        it.getType() + "," +
                        it.getQty() + "," +
                        it.getVolunteerName()
                );
            }

            System.out.println("Donations exported successfully to " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error exporting donations");
        }
    }

    // ================== EXPORT ORDERS ==================
    public static void exportOrdersCsv(
            ObservableList<Order> orders,
            String path,
            String filter
    ) {
        if (filter == null) {
            filter = "All"; 
        }

        LocalDate now = LocalDate.now();

        try (FileWriter writer = new FileWriter(path)) {

            // ===== Header Row =====
            writer.append("Charity,Item,Quantity,Date\n");

            for (Order o : orders) {

                boolean include = switch (filter) {
                    case "Today" -> o.getDate().isEqual(now);
                    case "Last 2 Days" -> !o.getDate().isBefore(now.minusDays(2));
                    case "This Week" -> !o.getDate().isBefore(now.minusDays(7));
                    default -> true; 
                };

                if (include) {
                    writer.append(
                            o.getCharityUsername() + "," +
                            o.getItemName() + "," +
                            o.getQuantity() + "," +
                            o.getDate()
                    ).append("\n");
                }
            }

            writer.flush();

        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    // ================== EXPORT ANY TABLE (ADMIN) ==================
    public static void exportListCsv(
            List<List<String>> data,
            List<String> headers,
            String path
    ) {
        try (PrintWriter pw = new PrintWriter(path)) {

            // ===== HEADER ROW =====
            pw.println(String.join(",", headers));

            // ===== DATA ROWS =====
            for (List<String> row : data) {
                pw.println(String.join(",", row));
            }

            System.out.println("Excel file exported successfully to " + path);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to export CSV.");
        }
    }

}
