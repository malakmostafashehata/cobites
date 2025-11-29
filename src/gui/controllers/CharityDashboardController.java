package gui.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import backend.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.Map;
import java.util.Optional;

public class CharityDashboardController {
    @FXML private FlowPane donationsCardsContainer;

    @FXML private VBox donationsSection, historySection, complaintsSection, profileSection;
    @FXML private VBox historyCardsContainer;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<FoodType> cbTypeFilter;
    @FXML private ComboBox<String> cbHistoryFilter;
    @FXML private TextArea txtComplaintDesc;
    @FXML private Button btnSubmitComplaint;
    @FXML private Button btnDonations, btnHistory, btnComplaints, btnProfile, btnExit;
    @FXML private TextField txtProfileName;
    @FXML private PasswordField txtProfilePassword;
    @FXML private TextField txtProfileAddress;
    @FXML private Button btnSaveProfile, btnDeleteProfile, btnLogoutProfile;
    @FXML private Button btnExportHistory;

    private ComplaintManager complaintManager;
    private ObservableList<Donation> donations = FXCollections.observableArrayList();
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML private Label charityNameLabel;
    private Charity charity; // الجمعية المحلية

    public void initData(Charity loggedCharity, ComplaintManager complaintManager) {
        this.charity = loggedCharity;
        this.complaintManager = complaintManager;
        charityNameLabel.setText(charity.getName());

        // تحميل جميع المستخدمين
        Map<String, User> users = FileManager.loadUsers();

        // تحميل التبرعات وربطها بالمستخدمين
        donations.setAll(FileManager.loadDonations(users));
        refreshDonationCards();

        // إعداد فلتر الهستوري
        cbHistoryFilter.getItems().addAll("All","Today","Last 2 Days","This Week");
        cbHistoryFilter.getSelectionModel().selectFirst();
        cbHistoryFilter.valueProperty().addListener((obs, oldV, newV) -> refreshHistoryCards());

        // بيانات الملف الشخصي
        txtProfileName.setText(charity.getName());
        txtProfilePassword.setText(charity.getPassword());
        txtProfileAddress.setText(charity.getAddress());

        // أزرار القائمة الجانبية
        btnDonations.setOnAction(e -> showSection("donations"));
        btnHistory.setOnAction(e -> showSection("history"));
        btnComplaints.setOnAction(e -> showSection("complaints"));
        btnProfile.setOnAction(e -> showSection("profile"));
        btnExit.setOnAction(e -> Platform.exit());

        // تقديم الشكاوى
        btnSubmitComplaint.setOnAction(e -> submitComplaint());

        // حفظ/حذف/تسجيل خروج الملف الشخصي
        btnSaveProfile.setOnAction(e -> saveProfile());
        btnDeleteProfile.setOnAction(e -> deleteProfile());
        btnLogoutProfile.setOnAction(e -> logout());

        // تصدير الهستوري
        btnExportHistory.setOnAction(e -> exportHistoryCsv());

        refreshHistoryCards();
    }

    private void showSection(String sec){
        donationsSection.setVisible(false);
        historySection.setVisible(false);
        complaintsSection.setVisible(false);
        profileSection.setVisible(false);

        switch(sec){
            case "donations" -> donationsSection.setVisible(true);
            case "history" -> historySection.setVisible(true);
            case "complaints" -> complaintsSection.setVisible(true);
            case "profile" -> profileSection.setVisible(true);
        }
    }

    // ------------------ عرض التبرعات ------------------
 // ------------------ عرض التبرعات ------------------
    private void refreshDonationCards() {
        donationsCardsContainer.getChildren().clear();

        for (Donation d : donations) {
            if (d.getItem().getQty() <= 0) continue; // لو الكمية 0 أو أقل، الكرت لا يظهر

            VBox card = new VBox(10);
            card.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-width: 1; -fx-background-color: #f9f9f9;");

            // صورة التبرع
            ImageView imgView = new ImageView();
            if (d.getItem().getImagePath() != null) {
                File imgFile = new File(d.getItem().getImagePath());
                if (imgFile.exists()) {
                    Image img = new Image(imgFile.toURI().toString(), 100, 100, true, true);
                    imgView.setImage(img);
                }
            }

            Label itemLabel = new Label("Item: " + d.getItem().getName());
            Label qtyLabel = new Label("Qty: " + d.getItem().getQty());

            // زر الطلب
            Button btnOrder = new Button("Order");
            btnOrder.setOnAction(e -> orderDonation(d));

            card.getChildren().addAll(imgView, itemLabel, qtyLabel, btnOrder);
            donationsCardsContainer.getChildren().add(card);
        }
    }

    // ------------------ طلب التبرع ------------------
    private void orderDonation(Donation d){
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Place Order");
        dialog.setHeaderText("Enter quantity:");
        Optional<String> res = dialog.showAndWait();
        res.ifPresent(qtyStr -> {
            try{
                int qty = Integer.parseInt(qtyStr);
                if(qty <= 0 || qty > d.getItem().getQty()){
                    showAlert("Invalid quantity");
                    return;
                }

                // إنشاء الأوردر
                Order o = new Order(charity.getName(), d.getItem().getName(), qty, LocalDate.now());
                FileManager.saveOrder(o); // حفظ الأوردر في الملف
                orders.add(o);
                addOrderToHistory(o); // يظهر فورًا في الهستوري

                // خصم الكمية من الشاشة فقط
                d.getItem().setQty(d.getItem().getQty() - qty);
                refreshDonationCards(); // لو الكمية 0، الكرت يختفي تلقائيًا

                showAlert("Order placed!");
            } catch(NumberFormatException ex){
                showAlert("Invalid number");
            }
        });
    }

    // ------------------ إضافة أوردر للهستوري فورًا ------------------
    private void addOrderToHistory(Order o){
        HBox card = new HBox(10);
        card.getChildren().add(new Label(o.getCharityName() + " | " + o.getItemName() + " | " + o.getQuantity() + " | " + o.getDate().format(dtf)));
        historyCardsContainer.getChildren().add(card);
    }
    // ------------------ تحديث الهستوري عند تغيير الفلتر ------------------
    private void refreshHistoryCards(){
        historyCardsContainer.getChildren().clear();
        String filter = cbHistoryFilter.getValue();
        LocalDate now = LocalDate.now();

        for(Order o : orders){
            if(!o.getCharityName().equals(charity.getName())) continue;

            boolean show = switch(filter){
                case "Today" -> o.getDate().isEqual(now);
                case "Last 2 Days" -> !o.getDate().isBefore(now.minusDays(2));
                case "This Week" -> !o.getDate().isBefore(now.minusDays(7));
                default -> true;
            };

            if(!show) continue;

            HBox card = new HBox(10);
            card.getChildren().add(new Label(o.getCharityName() + " | " + o.getItemName() + " | " + o.getQuantity() + " | " + o.getDate().format(dtf)));
            historyCardsContainer.getChildren().add(card);
        }
    }

    private void exportHistoryCsv(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Orders to CSV");
        File f = fc.showSaveDialog(historyCardsContainer.getScene().getWindow());
        if(f == null) return;
        ReportGenerator.exportOrdersCsv(orders, f.getAbsolutePath(), null);
        showAlert("Exported to " + f.getName());
    }

    // ------------------ الشكاوى ------------------
    private void submitComplaint(){
        String desc = txtComplaintDesc.getText();
        if(desc.isBlank()){ showAlert("Complaint cannot be empty"); return; }

        Complaint c = new Complaint(String.valueOf(System.currentTimeMillis()), charity.getName(), desc);
        complaintManager.addComplaint(c);
        FileManager.saveComplaint(c);
        txtComplaintDesc.clear();
        showAlert("Complaint submitted!");
    }

    // ------------------ الملف الشخصي ------------------
    private void saveProfile(){
        charity.setName(txtProfileName.getText());
        charity.setPassword(txtProfilePassword.getText());
        charity.setAddress(txtProfileAddress.getText());
        FileManager.saveUsers(UserManager.getInstance().getAllUsers());
        showAlert("Profile saved!");
    }

    private void deleteProfile(){
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete your account?",
                ButtonType.YES, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(btn -> {
            if(btn == ButtonType.YES){
                UserManager.getInstance().deleteUser(charity);
                FileManager.saveUsers(UserManager.getInstance().getAllUsers());
                Stage stage = (Stage) txtProfileName.getScene().getWindow();
                stage.close();
            }
        });
    }

    private void logout(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtProfileName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e){
            e.printStackTrace();
            showAlert("Logout failed");
        }
    }

    private void showAlert(String msg){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }
}
