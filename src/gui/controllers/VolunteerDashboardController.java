package gui.controllers;

import backend.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class VolunteerDashboardController {
	@FXML private Button btnEditProfile;
	@FXML private Button btnLogout;
    @FXML private VBox cardsContainer;
    @FXML private Label lblWelcome;
    @FXML private Label lblVolunteerName;
    @FXML private Button btnAddDonation;
    @FXML private Button btnExportCSV;
    @FXML private Button btnExit;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<FoodType> cbTypeFilter;
    @FXML private Label statusLabel;
   

    private Volunteer volunteer;
    private NotificationManager notificationManager;
    private ObservableList<FoodItem> donations = FXCollections.observableArrayList();
    private FilteredList<FoodItem> filteredDonations;

    public void initData(Volunteer volunteer, NotificationManager nm) {
        this.volunteer = volunteer;
        this.notificationManager = nm;

        lblWelcome.setText("WElCOME TO COBITES");
        lblVolunteerName.setText(volunteer.getName());

        donations.setAll(volunteer.getDonations().stream().map(Donation::getItem).toList());
        filteredDonations = new FilteredList<>(donations, p -> true);

        txtSearch.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        cbTypeFilter.getItems().setAll(FoodType.values());
        cbTypeFilter.getItems().add(0, null);
        cbTypeFilter.valueProperty().addListener((obs, oldV, newV) -> applyFilters());
        btnExit.setOnAction(e -> System.exit(0));
        btnEditProfile.setOnAction(e -> openEditProfile());
        btnLogout.setOnAction(e -> logout());


        // Add donation
        btnAddDonation.setOnAction(e -> openDonationForm(null));
        refreshCards();
        status("Ready");
    }

    private void refreshCards() {
        cardsContainer.getChildren().clear();
        applyFilters();
    }

    private void applyFilters() {
        String search = txtSearch.getText() != null ? txtSearch.getText().toLowerCase() : "";
        FoodType type = cbTypeFilter.getValue();

        filteredDonations.setPredicate(item -> {
            boolean matchesType = (type == null) || item.getType() == type;
            boolean matchesSearch = search.isBlank() ||
                    item.getName().toLowerCase().contains(search) ||
                    item.getVolunteerName().toLowerCase().contains(search);
            return matchesType && matchesSearch;
        });

        cardsContainer.getChildren().setAll(
                filteredDonations.stream().map(this::createCard).toList()
        );
    }

    private void openEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/EditProfile.fxml"));
            Parent root = loader.load();

            EditProfileController controller = loader.getController();
            controller.initData(volunteer);

            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(new Scene(root));
            stage.showAndWait();  // هذا ينتظر لغاية ما الفورم يغلق

            // بعد اغلاق الفورم، حدث الاسم ورقم الهاتف على الـ Dashboard
            lblVolunteerName.setText(volunteer.getName());

            // لو عندك Label للـ phone، حدثه هنا
            Label lblPhone = (Label) cardsContainer.getScene().lookup("#lblPhone");
            if (lblPhone != null) {
                lblPhone.setText(volunteer.getPhone());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private HBox createCard(FoodItem item) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("card"); // ✅ الكلاس اللي CSS هيشتغل عليه

        ImageView iv = new ImageView();
        if (item.getImagePath() != null && !item.getImagePath().isEmpty() && new File(item.getImagePath()).exists()) {
            Image img = new Image(new File(item.getImagePath()).toURI().toString(), 60, 60, true, true);
            iv.setImage(img);
        }

        VBox details = new VBox(5);
        Label name = new Label(item.getName());
        name.setStyle("-fx-font-weight:bold; -fx-font-size:14;");
        Label typeLabel = new Label(item.getType().toString());
        Label qty = new Label("Qty: " + item.getQty());

        // New date label, formatted as yyyy-MM-dd
        String dateStr = "";
        if (item.getDonationDate() != null) {
            dateStr = item.getDonationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        Label dateLabel = new Label(dateStr);

        details.getChildren().addAll(name, typeLabel, qty, dateLabel);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-blue"); // لو عايزة ستايل الزرار من CSS
        editBtn.setOnAction(e -> openDonationForm(item));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(iv, details, spacer, editBtn);
        return card;
    }

    private void openDonationForm(FoodItem existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/DonationForm.fxml"));
            Parent root = loader.load();
            DonationFormController controller = loader.getController();
            controller.initData(volunteer, notificationManager, existing);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(existing == null ? "Add Donation" : "Edit Donation");
            stage.showAndWait();

            donations.setAll(volunteer.getDonations().stream().map(Donation::getItem).toList()); // تحديث بعد أي تعديل
            refreshCards();
            status(existing == null ? "Donation added" : "Donation updated");
        } catch (Exception e) {
            e.printStackTrace();
            status("Error opening donation form", true);
        }
    }

    @FXML
    private void exportCsv() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export donations to CSV");
        File f = fc.showSaveDialog(cardsContainer.getScene().getWindow());
        if (f == null) return;

        ReportGenerator.exportDonationsCsv(donations, f.getAbsolutePath());
        status("Exported to " + f.getName());
    }

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblVolunteerName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            status("Logout failed", true);
        }
    }

    private void status(String text) {
        status(text, false);
    }

    private void status(String text, boolean error) {
        Platform.runLater(() -> {
            statusLabel.setText(text);
            statusLabel.setStyle(error ? "-fx-text-fill:red;" : "-fx-text-fill:green;");
        });
    }
}