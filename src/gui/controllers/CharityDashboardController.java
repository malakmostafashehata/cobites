package gui.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CharityDashboardController {
    @FXML private ComboBox<String> cbHistoryStatusFilter;

    // ---------- FXML Components ----------
    @FXML private FlowPane donationsCardsContainer;
    @FXML private VBox donationsSection, historySection, complaintsSection, profileSection;
    @FXML private VBox historyCardsContainer;
    @FXML private TextField txtSearch, txtProfileName, txtProfileAddress;
    @FXML private PasswordField txtProfilePassword;
    @FXML private TextInputControl txtProfilePhone;
    @FXML private ComboBox<String> cbTypeFilter, cbHistoryFilter;
    @FXML private TextArea txtComplaintDesc;
    @FXML private Button btnTopLogout, btnDonations, btnHistory, btnComplaints, btnProfile, btnExit;
    @FXML private Button btnSubmitComplaint, btnSaveProfile, btnDeleteProfile, btnExportHistory;
    @FXML private Label charityNameLabel, statusLabel;

    // ---------- Data ----------
    private Charity charity;  // Logged-in charity
    private ObservableList<Donation> donations = FXCollections.observableArrayList();
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private FilteredList<Donation> filteredDonations;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ---------- Initialization ----------
    public void initData(Charity loggedCharity) {
        this.charity = loggedCharity;

        // إعداد الـ ComboBoxes
        cbHistoryStatusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Completed"));
        cbHistoryStatusFilter.getSelectionModel().selectFirst();
        cbHistoryStatusFilter.valueProperty().addListener((obs, oldV, newV) -> refreshHistoryCards());

        cbHistoryFilter.setItems(FXCollections.observableArrayList("All", "Today", "Last 2 Days", "This Week"));
        cbHistoryFilter.getSelectionModel().selectFirst();
        cbHistoryFilter.valueProperty().addListener((obs, oldV, newV) -> refreshHistoryCards());



        // Load users and donations
        Map<String, User> users = FileManager.loadUsers();
        donations.setAll(FileManager.loadCharityStack(users));
        filteredDonations = new FilteredList<>(donations, p -> true);

        charityNameLabel.setText(charity.getName());

        // Setup type filter for donations
        ObservableList<String> typeOptions = FXCollections.observableArrayList("Select Type");
        for (FoodType ft : FoodType.values()) typeOptions.add(ft.name());
        cbTypeFilter.setItems(typeOptions);
        cbTypeFilter.getSelectionModel().selectFirst();

        // Add listeners for search and type filtering
        txtSearch.textProperty().addListener((obs, oldV, newV) -> applyFilters());
        cbTypeFilter.valueProperty().addListener((obs, oldV, newV) -> applyFilters());

        // Load all orders
        orders.setAll(FileManager.loadOrders(users));

        // Update order addresses and charity username if missing
        Map<String, Donation> donationMap = new HashMap<>();
        for (Donation d : donations) donationMap.put(d.getItem().getName(), d);
        for (Order o : orders) {
            Donation d = donationMap.get(o.getItemName());
            if (d != null && (o.getDonorAddress() == null || o.getDonorAddress().isBlank())) {
                o.setDonorAddress(d.getDonorAddress());
                o.setDonorPhone(d.getDonorPhone());
            }
            if (o.getCharityUsername() == null || o.getCharityUsername().isBlank() ||
                    o.getCharityUsername().equalsIgnoreCase(charity.getName())) {
                o.setCharityUsername(charity.getUserName());
            }
        }

        // Refresh UI cards
        refreshDonationCards();
        refreshHistoryCards();

        // Show default section
        showSection("donations");

        // Prefill profile fields
        txtProfileName.setText(charity.getName());
        txtProfilePassword.setText(charity.getPassword());
        txtProfileAddress.setText(charity.getAddress());
        txtProfilePhone.setText(charity.getPhone());

        // ---------- Buttons ----------
        btnDonations.setOnAction(e -> showSection("donations"));
        btnHistory.setOnAction(e -> showSection("history"));
        btnComplaints.setOnAction(e -> showSection("complaints"));
        btnProfile.setOnAction(e -> showSection("profile"));
        btnExit.setOnAction(e -> Platform.exit());
        btnSubmitComplaint.setOnAction(e -> submitComplaintCharity());
        btnSaveProfile.setOnAction(e -> saveProfile());
        btnDeleteProfile.setOnAction(e -> deleteProfile());
        btnTopLogout.setOnAction(event -> logout());
        btnExportHistory.setOnAction(e -> exportHistoryCsv());
    }

    // ---------- Donations Filtering ----------
    private void applyFilters() {
        String search = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase();
        String selectedTypeName = cbTypeFilter.getValue();

        filteredDonations.setPredicate(item -> {
            boolean matchType = selectedTypeName.equals("Select Type") ||
                    item.getItem().getType().name().equals(selectedTypeName);
            boolean matchSearch = search.isBlank() || item.getItem().getName().toLowerCase().contains(search);
            return matchType && matchSearch;
        });

        donationsCardsContainer.getChildren().clear();
        for (Donation d : filteredDonations) {
            if (d.getItem().getQty() <= 0) continue;
            donationsCardsContainer.getChildren().add(createDonationCard(d));
        }
    }

    // ---------- Donation Card UI ----------
    private VBox createDonationCard(Donation d) {
        VBox card = new VBox(10);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-color: transparent; -fx-border-width: 1;");

        // Image
        ImageView imgView = new ImageView();
        if (d.getItem().getImagePath() != null) {
            File imgFile = new File(d.getItem().getImagePath());
            if (imgFile.exists()) imgView.setImage(new Image(imgFile.toURI().toString(), 100, 100, true, true));
        }

        // Labels
        Label itemLabel = new Label("Item: " + d.getItem().getName());
        itemLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label qtyLabel = new Label("Quantity: " + d.getItem().getQty());
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Order Button
        Button btnOrder = new Button("Order");
        btnOrder.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 15;");
        btnOrder.setOnAction(e -> orderDonation(d));

        card.getChildren().addAll(imgView, itemLabel, qtyLabel, btnOrder);
        return card;
    }

    // ---------- Show Selected Section ----------
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

    // ---------- Refresh Donations Cards ----------
    private void refreshDonationCards() {
        donationsCardsContainer.getChildren().clear();
        for (Donation d : donations) {
            if (d.getItem().getQty() <= 0) continue;
            donationsCardsContainer.getChildren().add(createDonationCard(d));
        }
    }

    // ---------- Place Donation Order ----------
    private void orderDonation(Donation d) {
        Stage orderStage = new Stage();
        orderStage.setTitle("Place Order");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #162447; -fx-background-radius: 10; -fx-border-radius: 10;");

        Label lbl = new Label("Enter quantity for: " + d.getItem().getName());
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        TextField txtQty = new TextField("1");
        txtQty.setPromptText("Quantity");
        txtQty.setStyle("-fx-background-radius: 8; -fx-padding: 5;");

        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        Button btnConfirm = new Button("Confirm");
        Button btnCancel = new Button("Cancel");

        // Styles
        String btnStyle = "-fx-background-color: #162447; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 5 15;";
        String btnHover = "-fx-background-color: #1F3B73; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 8; -fx-padding: 5 15;";

        btnConfirm.setStyle(btnStyle);
        btnCancel.setStyle(btnStyle);
        btnConfirm.setOnMouseEntered(e -> btnConfirm.setStyle(btnHover));
        btnConfirm.setOnMouseExited(e -> btnConfirm.setStyle(btnStyle));
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle(btnHover));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle(btnStyle));

        buttons.getChildren().addAll(btnConfirm, btnCancel);
        root.getChildren().addAll(lbl, txtQty, buttons);

        Scene scene = new Scene(root, 300, 180);
        orderStage.setScene(scene);
        orderStage.initOwner(donationsCardsContainer.getScene().getWindow());
        orderStage.show();

        // Confirm order
        btnConfirm.setOnAction(e -> {
            try {
                int qty = Integer.parseInt(txtQty.getText());
                if (qty <= 0 || qty > d.getItem().getQty()) {
                    showStyledAlert("Invalid quantity", true);
                    return;
                }

                // Update donation quantity
                d.getItem().setQty(d.getItem().getQty() - qty);
                FileManager.updateCharityStack(new ArrayList<>(donations));

                // Create new order
                String orderId = String.valueOf(10000000 + (int)(Math.random() * 90000000));
                Order o = new Order(orderId,
                        charity.getUserName(),
                        d.getItem().getName(),
                        qty,
                        LocalDate.now(),
                        charity.getAddress(),
                        d.getDonorAddress(),
                        charity.getPhone(),
                        d.getDonorPhone(),
                        d.getDonor() != null ? d.getDonor().getUserName() : "Unknown");
                o.setStatus("Pending");

                orders.add(o);
                FileManager.saveOrder(o);

                // Refresh UI
                donations.setAll(FileManager.loadCharityStack(FileManager.loadUsers()));
                refreshDonationCards();
                refreshHistoryCards();

                orderStage.close();
                Platform.runLater(() -> status("Order placed!", false));

            } catch (NumberFormatException ex) {
                status("Invalid number", true);
            }
        });

        btnCancel.setOnAction(e -> orderStage.close());
    }

    // ---------- Refresh Order History ----------
    private void refreshHistoryCards() {
        historyCardsContainer.getChildren().clear();
        if (orders == null) return;

        String timeFilter = cbHistoryFilter.getValue();
        if (timeFilter == null) timeFilter = "All";

        String statusFilter = cbHistoryStatusFilter.getValue();
        if (statusFilter == null) statusFilter = "All";

        LocalDate now = LocalDate.now();
        String currentUsername = charity.getUserName().trim();
        String currentName = charity.getName().trim();

        for (Order o : orders) {
            String orderCharity = o.getCharityUsername() != null ? o.getCharityUsername().trim() : "";
            if (!orderCharity.equalsIgnoreCase(currentUsername) && !orderCharity.equalsIgnoreCase(currentName))
                continue;

            boolean show = switch (timeFilter) {
            case "Today" ->
                    o.getDate().isEqual(now);

            case "Last 2 Days" ->
                    !o.getDate().isBefore(now.minusDays(2));

            case "This Week" -> {
                LocalDate startOfWeek =
                        now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield !o.getDate().isBefore(startOfWeek) && !o.getDate().isAfter(now);
            }

            default -> true; // All
        };

            boolean matchesStatus = statusFilter.equals("All") || o.getStatus().equalsIgnoreCase(statusFilter);

            if (!show || !matchesStatus) continue;

            // Card
            HBox card = new HBox(10);
            card.setPadding(new Insets(10));
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: black;");


            card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 10;");
            // Split text into parts
            String prefix = "Item: " + o.getItemName() +
                    " | Qty: " + o.getQuantity() +
                    " | Date: " + o.getDate().format(dtf) +
                    " | Status: ";

            String status = o.getStatus();

// Create Text nodes
            Text tPrefix = new Text(prefix);
            tPrefix.setStyle("-fx-fill: #162447; -fx-font-weight: bold;");

            Text tStatus = new Text(status);
            if ("Pending".equalsIgnoreCase(status)) {
                tStatus.setStyle("-fx-fill: red; -fx-font-weight: bold;");
            } else if ("Completed".equalsIgnoreCase(status)) {
                tStatus.setStyle("-fx-fill: green; -fx-font-weight: bold;");
            } else {
                tStatus.setStyle("-fx-fill: #162447; -fx-font-weight: bold;");
            }

// Combine into TextFlow
            TextFlow textFlow = new TextFlow(tPrefix, tStatus);

// Add to card
            card.getChildren().add(textFlow);




            if ("Pending".equalsIgnoreCase(o.getStatus())) {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button btnDelete = new Button("X");
                btnDelete.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold; "
                        + "-fx-background-radius: 50%; -fx-min-width: 25px; -fx-min-height: 25px; "
                        + "-fx-max-width: 25px; -fx-max-height: 25px;");
                // Hover effect
                btnDelete.setOnMouseEntered(ev ->
                        btnDelete.setStyle("-fx-background-color: darkred; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-min-width: 25px; -fx-min-height: 25px; -fx-max-width: 25px; -fx-max-height: 25px;")
                );

                btnDelete.setOnMouseExited(ev ->
                        btnDelete.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-min-width: 25px; -fx-min-height: 25px; -fx-max-width: 25px; -fx-max-height: 25px;")
                );
                btnDelete.setOnAction(e -> showDeleteConfirmation(o));

                card.getChildren().addAll(spacer, btnDelete);
            }

            historyCardsContainer.getChildren().add(card);
        }

        if (historyCardsContainer.getChildren().isEmpty()) {
            Label emptyLbl = new Label("No orders found for the selected filter.");
            emptyLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            historyCardsContainer.getChildren().add(emptyLbl);
        }
    }

    // ---------- Delete Confirmation ----------
    private void showDeleteConfirmation(Order o) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Delete");

        VBox box = new VBox(20);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #162447; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label message = new Label("Are you sure to delete this order?");
        message.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox buttons = new HBox(20);
        buttons.setAlignment(Pos.CENTER);

        Button yesBtn = new Button("Yes");
        yesBtn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-padding: 5 20;");
        // Hover effect
        yesBtn.setOnMouseEntered(ev ->
                yesBtn.setStyle("-fx-background-color: #1F3B73; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-padding: 5 20;")
        );

        yesBtn.setOnMouseExited(ev ->
                yesBtn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-padding: 5 20;")
        );
        yesBtn.setOnAction(ev -> {
            orders.remove(o);
            FileManager.removeOrder(o);
            refreshHistoryCards();
            donations.setAll(FileManager.loadCharityStack(FileManager.loadUsers()));
            refreshDonationCards();
            dialog.close();
            status("Order deleted!", false);
        });

        Button noBtn = new Button("No");
        noBtn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-padding: 5 20; ");
        // Hover effect
        noBtn.setOnMouseEntered(ev ->
                noBtn.setStyle("-fx-background-color: #1F3B73; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-padding: 5 20;")
        );

        noBtn.setOnMouseExited(ev ->
                noBtn.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-padding: 5 20;")
        );
        noBtn.setOnAction(ev -> dialog.close());

        buttons.getChildren().addAll(yesBtn, noBtn);
        box.getChildren().addAll(message, buttons);

        Scene scene = new Scene(box);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ---------- Export History ----------
    private void exportHistoryCsv(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Orders to CSV");
        File f = fc.showSaveDialog(historyCardsContainer.getScene().getWindow());
        if(f == null) return;
        ReportGenerator.exportOrdersCsv(orders, f.getAbsolutePath(), null);
    }

    // ---------- Complaints ----------
    private void submitComplaintCharity() {
        String desc = txtComplaintDesc.getText();

        if (desc == null || desc.trim().isEmpty()) {
            showStyledAlert("Complaint cannot be empty!", true);
            return;
        }

        String role = charity.getClass().getSimpleName(); 
        String username = charity.getName();             
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


    private void showStyledAlert(String message, boolean isError) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #162447; -fx-border-radius: 10; -fx-background-radius: 10;");
        content.setAlignment(Pos.CENTER);

        Label lblMessage = new Label(message);
        lblMessage.setWrapText(true);
        lblMessage.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        lblMessage.setAlignment(Pos.CENTER);

        content.getChildren().add(lblMessage);

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setStyle("-fx-background-color: #162447; -fx-border-radius: 10; -fx-background-radius: 10;");
        alert.getDialogPane().setMinWidth(350);
        alert.getDialogPane().setMinHeight(150);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okButtonType);

        Button btnOk = (Button) alert.getDialogPane().lookupButton(okButtonType);
        btnOk.setStyle("-fx-background-color: #162447; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-border-width: 2; -fx-padding: 5 20;");
        alert.showAndWait();
    }

    // ---------- Profile Management ----------
    private void saveProfile() {
        UserManager um = UserManager.getInstance();

        String newName = txtProfileName.getText().trim();
        String newPhone = txtProfilePhone.getText().trim();
        String newPassword = txtProfilePassword.getText();
        String newAddress = txtProfileAddress.getText().trim();

        if (newName.isEmpty()  || newPhone.isEmpty() || newPassword.isEmpty() || newAddress.isEmpty()) {
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

        charity.setName(newName);
        charity.setPhone(newPhone);
        charity.setPassword(newPassword);
        charity.setAddress(newAddress);
        FileManager.saveUsers(um.getAllUsers());
        showStyledAlert("Profile saved successfully!", false);
    }

    private void deleteProfile() {
        // Create content for the alert
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #162447;"); // dark background

        Label lblMessage = new Label("Are you sure you want to delete your account?");
        lblMessage.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        content.getChildren().add(lblMessage);

        // Create the alert
        Alert confirm = new Alert(Alert.AlertType.NONE);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.setGraphic(null);
        confirm.getDialogPane().setContent(content);
        confirm.getDialogPane().setStyle("-fx-background-color: #162447;"); // match dark background

        // Buttons
        ButtonType yesButtonType = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yesButtonType, cancelButtonType);

        // Get actual buttons
        Button btnYes = (Button) confirm.getDialogPane().lookupButton(yesButtonType);
        Button btnCancel = (Button) confirm.getDialogPane().lookupButton(cancelButtonType);

        // Button styles: Rounded rectangle
        String transparentStyle = "-fx-background-color: transparent; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: white; " +
                "-fx-border-width: 2; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +  // slightly rounded corners
                "-fx-border-radius: 8; " +
                "-fx-padding: 5 20;";

        String hoverStyle = "-fx-background-color: #1F3B73; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: white; " +
                "-fx-border-width: 2; " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8;";

        // Apply styles and hover effect
        btnYes.setStyle(transparentStyle);
        btnCancel.setStyle(transparentStyle);

        btnYes.setOnMouseEntered(e -> btnYes.setStyle(hoverStyle));
        btnYes.setOnMouseExited(e -> btnYes.setStyle(transparentStyle));
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle(hoverStyle));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle(transparentStyle));

        // Handle button clicks
        confirm.showAndWait().ifPresent(response -> {
            if (response == yesButtonType) {
                UserManager.getInstance().deleteUser(charity);
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



    // ---------- Logout ----------
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
        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait());
    }

    // ---------- Status Label ----------
    private void status(String t, boolean error) {
        Platform.runLater(() -> {
            statusLabel.setText(t);
            statusLabel.setStyle(error ? "-fx-text-fill:red;" : "-fx-text-fill:green;");
        });
    }
}