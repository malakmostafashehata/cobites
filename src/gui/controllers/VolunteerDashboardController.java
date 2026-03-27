package gui.controllers;

import backend.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Controller for the Volunteer Dashboard view
public class VolunteerDashboardController {

    // ===== Profile Section =====
    @FXML private VBox profileSection; // Container for profile editing

    @FXML private TextField txtProfileName;
    @FXML private TextField txtProfilePhone;
    @FXML private TextField txtProfilePassword;
    @FXML private TextField txtProfileAddress;

    @FXML private Button btnSaveProfile;
    @FXML private Button btnDeleteProfile;
    @FXML private Button btnEditProfile;

    // ===== Main Dashboard Components =====
    @FXML private FlowPane donationsCardsContainer; // Container to display donation cards
    @FXML private Label lblWelcome;
    @FXML private Label lblVolunteerName;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<FoodType> cbTypeFilter; // Filter by food type
    @FXML private ComboBox<String> cbHistoryFilter; // Filter by donation date
    @FXML private Label statusLabel;
    @FXML private Button btnAddDonation;
    @FXML private Button btnExportCSV;
    @FXML private Button btnLogout;
    @FXML private Button btnExit;
    @FXML private VBox donationsSection; // Container for donation list

    private Volunteer volunteer; // Current logged-in volunteer
    // ===== Complaints Section =====
    @FXML private VBox complaintsSection;
    @FXML private TextArea txtComplaintDesc;
    @FXML private Button btnSubmitComplaint;

    // ===== Sidebar Buttons =====
    @FXML private Button btnDonations;
    @FXML private Button btnComplaints;


    private ObservableList<FoodItem> donations = FXCollections.observableArrayList(); // All donations of this volunteer
    private FilteredList<FoodItem> filteredDonations; // Filtered list for search/type/date filters

    // ====================== INIT ===========================
    // Initialize the dashboard with the current volunteer data
    public void initData(Volunteer volunteer) {
        this.volunteer = volunteer;

        // Show Donations Section
        btnDonations.setOnAction(e -> showDonationsSection());
        // Show Complaints Section
        btnComplaints.setOnAction(e -> showComplaintsSection());
        // Show Profile Section
        btnEditProfile.setOnAction(e -> openProfileSection());

        // ===== Complaint Button =====   <-- ADDED
        btnSubmitComplaint.setOnAction(e -> submitComplaintVolunteer());

        // Profile buttons
        btnSaveProfile.setOnAction(e -> saveProfile());
        btnDeleteProfile.setOnAction(e -> deleteProfile());


        // Labels
        lblWelcome.setText("WELCOME TO COBITES");
        lblVolunteerName.setText(volunteer.getName());

        // Load donations and setup filters
        loadDonations();
        filteredDonations = new FilteredList<>(donations, p -> true);

        // Search and filter listeners
        txtSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        cbTypeFilter.getItems().setAll(FoodType.values());
        cbTypeFilter.getItems().add(0, null); // Allow "All" selection
        cbTypeFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbHistoryFilter.getItems().addAll("All", "Today", "Last 2 Days", "This Week");
        cbHistoryFilter.getSelectionModel().selectFirst();
        cbHistoryFilter.valueProperty().addListener((obs, o, n) -> applyFilters());

        // Other buttons
        btnLogout.setOnAction(e -> logout());
        btnExit.setOnAction(e -> System.exit(0));
        btnAddDonation.setOnAction(e -> openDonationForm(null));
        btnExportCSV.setOnAction(e -> exportCsv());

        // Apply filters initially and set status
        applyFilters();
        status("Ready");
    }

    // ====================== Donations ======================
    // Load donations from file and display them
    private void loadDonations() {
        donations.clear();

        // Map of all users for lookup
        Map<String, User> usersMap = UserManager.getInstance().getAllUsers().stream()
                .collect(Collectors.toMap(User::getUserName, u -> u));

        // Load all donations
        List<Donation> allDonations = FileManager.loadDonations(usersMap, volunteer);

        // Filter donations belonging to this volunteer
        for (Donation d : allDonations) {
            if (d.getDonor() != null && d.getDonor().getUserName().equals(volunteer.getUserName())) {
                donations.add(d.getItem());
            }
        }

        // Display donation cards
        donationsCardsContainer.getChildren().clear();
        donationsCardsContainer.getChildren().setAll(
                donations.stream().map(this::createCard).collect(Collectors.toList())
        );
    }

    // Apply search, type, and date filters to donations
    private void applyFilters() {
        String search = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase();
        FoodType type = cbTypeFilter.getValue();
        String history = cbHistoryFilter.getValue();

        filteredDonations.setPredicate(item -> {

            boolean matchType = type == null || item.getType() == type;

            boolean matchSearch =
                    search.isBlank() ||
                    item.getName().toLowerCase().contains(search) ||
                    item.getVolunteerName().toLowerCase().contains(search);

            boolean matchDate = true;

            if (!history.equals("All")) {
                var today = java.time.LocalDate.now();
                var date = item.getDonationDate();

                switch (history) {
                    case "Today" ->
                        matchDate = date.equals(today);

                    case "Last 2 Days" ->
                        matchDate = !date.isBefore(today.minusDays(2));

                    case "This Week" -> {
                        var startOfWeek = today.with(
                                java.time.temporal.TemporalAdjusters
                                        .previousOrSame(java.time.DayOfWeek.MONDAY)
                        );
                        matchDate = !date.isBefore(startOfWeek) && !date.isAfter(today);
                    }
                }
            }

            return matchType && matchSearch && matchDate;
        });

        donationsCardsContainer.getChildren().setAll(
                filteredDonations.stream().map(this::createCard).collect(Collectors.toList())
        );
    }

    // Create a visual card for a donation item
    private VBox createCard(FoodItem item) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("card");

        // ===== Image of donation =====
        ImageView iv = new ImageView();
        if (item.getImagePath() != null && new File(item.getImagePath()).exists()) {
            Image img = new Image(new File(item.getImagePath()).toURI().toString());
            iv.setImage(img);
            iv.setPreserveRatio(true);
            iv.setFitWidth(100);
            iv.setFitHeight(100);
            iv.setSmooth(true);
        }

        VBox info = new VBox(5);

        Label nameLabel = new Label(item.getName());
        Label typeLabel = new Label(item.getType().toString());

        Label qtyLabel = new Label("Quantity : " + item.getQty());

        Label dateLabel = new Label(item.getDonationDate().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        ));

        // ===== Remaining / Status =====
        int remaining = FileManager.getQuantityFromStock(item.getId());
        Label statusLabel = new Label();
        if (remaining > 0) {
            statusLabel.setText("Remaining: " + remaining);
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("All items donated");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }

        info.getChildren().addAll(nameLabel, typeLabel, qtyLabel, dateLabel, statusLabel);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        card.getChildren().addAll(iv, info, space);

        return card;
    }


    // Open form to add or edit a donation
    private void openDonationForm(FoodItem existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/DonationForm.fxml"));
            Parent root = loader.load();

            DonationFormController c = loader.getController();
            c.initData(volunteer, existing);

            Stage s = new Stage();
            s.setScene(new Scene(root));
            s.setTitle(existing == null ? "Add Donation" : "Edit Donation");
            s.showAndWait();

            loadDonations();
            applyFilters();
            status(existing == null ? "Donation added" : "Donation updated");
        } catch (Exception ex) {
            ex.printStackTrace();
            status("Error opening donation form", true);
        }
    }

    // ====================== Profile ======================
    @FXML
    private void openProfileSection() {
        profileSection.setVisible(true);
        donationsSection.setVisible(false);

        txtProfileName.setText(volunteer.getName());
        txtProfilePhone.setText(volunteer.getPhone());
        txtProfilePassword.setText(volunteer.getPassword());
        txtProfileAddress.setText(volunteer.getAddress());
    }

    private void hideProfile() {
        profileSection.setVisible(false);
        donationsSection.setVisible(true);
    }

    // Save profile changes and update donations info
    private void saveProfile() {
        UserManager um = UserManager.getInstance();

        String newName = txtProfileName.getText().trim();
        String newPhone = txtProfilePhone.getText().trim();
        String newPassword = txtProfilePassword.getText();
        String newAddress = txtProfileAddress.getText().trim();

        // Validate input
        if (newName.isEmpty() || newPhone.isEmpty() || newPassword.isEmpty() || newAddress.isEmpty()) {
            showStyledAlert("All fields are required!", true);
            return;
        }

        if (newPassword.length() < 4) {
            showStyledAlert("Password must be at least 4 characters!", true);
            return;
        }

        if (!newPhone.matches("\\d{11}")) {
            showStyledAlert("Phone must be 11 digits!", true);
            return;
        }

        // Update volunteer data
        volunteer.setName(newName);
        volunteer.setPhone(newPhone);
        volunteer.setPassword(newPassword);
        volunteer.setAddress(newAddress);

        // Save users
        FileManager.saveUsers(um.getAllUsers());

        // Update donations info for this volunteer
        Map<String, User> usersMap = um.getAllUsers().stream()
                .collect(Collectors.toMap(User::getUserName, u -> u));

        List<Donation> allDonations = FileManager.loadDonations(usersMap, volunteer);

        for (Donation d : allDonations) {
            if (d.getDonor() != null && d.getDonor().getUserName().equals(volunteer.getUserName())) {
                d.getItem().setDonorAddress(volunteer.getAddress());
                d.getItem().setDonorPhone(volunteer.getPhone());
                d.setDonorPhone(volunteer.getPhone());
            }
        }

        // Rewrite donations file and update orders
        FileManager.rewriteDonations(allDonations);
        FileManager.updateOrdersAfterProfileEdit(volunteer);
        showStyledAlert("Profile saved successfully!", false);
    }

    // Show a styled alert dialog
    private void showStyledAlert(String message, boolean isError) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #162447;");

        Label lblMessage = new Label(message);
        lblMessage.setStyle("-fx-text-fill: " + (isError ? "red" : "white") + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        content.getChildren().add(lblMessage);

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setStyle("-fx-background-color: #162447;");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButtonType);

        Button btnOk = (Button) alert.getDialogPane().lookupButton(okButtonType);
        String btnStyle = "-fx-background-color: #162447; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-border-width: 1.5; " +
                "-fx-font-weight: bold;";
        String btnHover = "-fx-background-color: #1F3B73; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-border-width: 1.5; " +
                "-fx-font-weight: bold;";

        btnOk.setStyle(btnStyle);
        btnOk.setOnMouseEntered(e -> btnOk.setStyle(btnHover));
        btnOk.setOnMouseExited(e -> btnOk.setStyle(btnStyle));

        alert.showAndWait();
    }

    // Delete the volunteer account
    private void deleteProfile() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #162447;");

        Label lblMessage = new Label("Are you sure you want to delete your account?");
        lblMessage.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        content.getChildren().add(lblMessage);

        Alert confirm = new Alert(Alert.AlertType.NONE);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.setGraphic(null);
        confirm.getDialogPane().setContent(content);
        confirm.getDialogPane().setStyle("-fx-background-color: #162447;");

        ButtonType yesButtonType = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yesButtonType, cancelButtonType);

        Button btnYes = (Button) confirm.getDialogPane().lookupButton(yesButtonType);
        Button btnCancel = (Button) confirm.getDialogPane().lookupButton(cancelButtonType);

        String btnStyle = "-fx-background-color: #162447; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-border-width: 1.5; " +
                "-fx-font-weight: bold;";
        String btnHover = "-fx-background-color: #1F3B73; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-border-width: 1.5; " +
                "-fx-font-weight: bold;";

        btnYes.setStyle(btnStyle);
        btnCancel.setStyle(btnStyle);

        btnYes.setOnMouseEntered(e -> btnYes.setStyle(btnHover));
        btnYes.setOnMouseExited(e -> btnYes.setStyle(btnStyle));
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle(btnHover));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle(btnStyle));

        confirm.showAndWait().ifPresent(response -> {
            if (response == yesButtonType) {
                UserManager.getInstance().deleteUser(volunteer);
                FileManager.saveUsers(UserManager.getInstance().getAllUsers());

                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/gui/views/Login.fxml"));
                    Stage stage = (Stage) txtProfileName.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Login");
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    // ====================== Other ======================
    // Export donations to CSV
    private void exportCsv() {
        FileChooser fc = new FileChooser();
        File f = fc.showSaveDialog(donationsCardsContainer.getScene().getWindow());
        if (f == null) return;

        ReportGenerator.exportDonationsCsv(donations, f.getAbsolutePath());
        status("Exported to " + f.getName());
    }

    // Logout and return to login screen
    private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/views/Login.fxml"));
            Stage stage = (Stage) lblVolunteerName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================== Section Navigation ======================
    private void showDonationsSection() {
        donationsSection.setVisible(true);
        complaintsSection.setVisible(false);
        profileSection.setVisible(false);
    }

    private void showComplaintsSection() {
        donationsSection.setVisible(false);
        complaintsSection.setVisible(true);
        profileSection.setVisible(false);
    }

    // ====================== Complaints ======================
    private void submitComplaintVolunteer() {
        String desc = txtComplaintDesc.getText();

        if (desc == null || desc.trim().isEmpty()) {
            showStyledAlert("Complaint cannot be empty!", true);
            return;
        }

        String role = volunteer.getClass().getSimpleName(); 
        String username = volunteer.getUserName();          

        Complaint complaint = new Complaint(
                String.valueOf(System.currentTimeMillis()), 
                role,
                username,
                desc.trim(),
                java.time.LocalDate.now(),
                "Pending"                                   
        );

        FileManager.saveComplaint(complaint);

        txtComplaintDesc.clear();
        showStyledAlert("Complaint submitted successfully!", false);
    }




    // Set status message
    private void status(String t) {
        status(t, false);
    }

    private void status(String t, boolean error) {
        Platform.runLater(() -> {
            statusLabel.setText(t);
            statusLabel.setStyle(error ? "-fx-text-fill:red;" : "-fx-text-fill:green;");
        });
    }
}