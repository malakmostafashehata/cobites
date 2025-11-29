package backend;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import javafx.collections.ObservableList;

public class ReportGenerator {

    // ================== Donations CSV ==================
    public static void exportDonationsCsv(List<FoodItem> donations, String fileName) {
        try (PrintWriter pw = new PrintWriter(fileName)) {
            for (FoodItem it : donations) {
                pw.printf(
                        "name: %s , type: %s , quantity: %d , volunteer: %s%n",
                        it.getName(),
                        it.getType(),
                        it.getQty(),
                        it.getVolunteerName()
                );
            }
        } catch(Exception e) {
            System.err.println("Error exporting donations: " + e.getMessage());
        }
    }

    // ================== Orders Report ==================
    public static void generateOrderReport(List<Order> orders, String fileName){
        try(PrintWriter pw = new PrintWriter(fileName)){
            pw.println("===== Orders Report =====");
            for(Order o : orders){
                pw.println("Order ID: " + o.getId());
                pw.println("  Charity: " + o.getCharityName());
                pw.println("  Item: " + o.getItemName());
                pw.println("  Quantity: " + o.getQuantity());
                pw.println("  Date: " + o.getDate());
                pw.println();
            }
        } catch(Exception e){
            System.err.println("Error generating order report: " + e.getMessage());
        }
    }

    // ================== Complaints Report ==================
    public static void generateComplaintReport(List<Complaint> complaints, String fileName){
        try(PrintWriter pw = new PrintWriter(fileName)){
            pw.println("===== Complaints Report =====");
            for(Complaint c : complaints){
                pw.println("Complaint ID: " + c.getId());
                pw.println("  Charity: " + c.getCharityName());
                pw.println("  Date: " + c.getDate());
                pw.println("  Description: " + c.getDescription());
                pw.println();
            }
        } catch(Exception e){
            System.err.println("Error generating complaint report: " + e.getMessage());
        }
    }

    // ================== Notifications Report ==================
    public static void generateNotificationReport(NotificationManager nm, String fileName){
        try(PrintWriter pw = new PrintWriter(fileName)){
            pw.println("===== Notifications Report =====");
            for(String n : nm.getAll()){
                pw.println(n);
            }
        } catch(Exception e){
            System.err.println("Error generating notification report: " + e.getMessage());
        }
    }

    /**
     @param orders        قائمة كل الأوردرات
     @param absolutePath  مسار الملف الذي سيتم إنشاءه
    @param filter        فلتر التاريخ: "All", "Today", "Last 2 Days", "This Week"
    */
   public static void exportOrdersCsv(ObservableList<Order> orders, String absolutePath, String filter) {
       LocalDate now = LocalDate.now();

       try (FileWriter writer = new FileWriter(absolutePath)) {
           // كتابة الهيدر
           writer.append("Charity Name,Item Name,Quantity,Date\n");

           // تصدير الأوردرات مع تطبيق الفلتر
           for (Order o : orders) {
               boolean include = switch (filter) {
                   case "Today" -> o.getDate().isEqual(now);
                   case "Last 2 Days" -> !o.getDate().isBefore(now.minusDays(2));
                   case "This Week" -> !o.getDate().isBefore(now.minusDays(7));
                   default -> true; // "All"
               };

               if (include) {
                   writer.append(o.getCharityName()).append(",");
                   writer.append(o.getItemName()).append(",");
                   writer.append(String.valueOf(o.getQuantity())).append(",");
                   writer.append(o.getDate().toString()).append("\n");
               }
           }

           writer.flush();
           System.out.println("Export successful to " + absolutePath);
       } catch (IOException e) {
           e.printStackTrace();
           System.err.println("Failed to export orders to CSV.");
       }
   
}

}
