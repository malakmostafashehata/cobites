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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

public class DeliveryDashboardController {
	@FXML private ComboBox<String> cbHistoryFilter; // الجديد
    // ---------- FXML Components ----------
    @FXML private Button btnLogout, btnExit, btnExportCSV, btnEditProfile, btnSaveProfile, btnDeleteProfile;
    @FXML private Button btnOrder, btnHistory, btnComplaints, btnSubmitComplaint;
    @FXML private Label lblWelcome, lblDeliveryName;
    @FXML private TextField txtSearch, txtProfileName, txtProfilePhone, txtProfileAddress, txtHistorySearch;
    @FXML private PasswordField txtProfilePassword;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TableView<Order> deliveryTable, historyTable;
    @FXML private TableColumn<Order, String> colId, colDonorAddress, colDonorPhone, colCharityAddress, colCharityPhone, colStatus, colDate;
    @FXML private TableColumn<Order, Void> colAction;
    @FXML private TableColumn<Order, String> colHistoryId, colHistoryDonorAddress, colHistoryDonorPhone,
            colHistoryCharityAddress, colHistoryCharityPhone, colHistoryDate, colHistoryStatus;
    @FXML private VBox profileSection, ordersSection, complaintsSection, historySection;
    @FXML private TextArea txtComplaintDesc;

    // ---------- Data Fields ----------
    private User deliveryUser;
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private FilteredList<Order> filteredOrders;
    private ObservableList<Order> historyOrders = FXCollections.observableArrayList();
    private FilteredList<Order> filteredHistoryOrders;

    // ---------- Initialize ----------
    public void initData(User user) {
        this.deliveryUser = user;
        lblDeliveryName.setText(user.getName());
    }

    public void initialize() {
        setupOrdersSection();
        setupHistorySection();
        setupProfileSection();
        setupComplaintsSection();
        setupButtons();
        loadOrders();
        setupHistorySection();
        setupHistoryFilter(); 
    }

    // ---------- Buttons ----------
    private void setupButtons() {
        btnOrder.setOnAction(e -> showOrdersSection());
        btnHistory.setOnAction(e -> showHistorySection());
        btnComplaints.setOnAction(e -> showComplaintsSection());
        btnEditProfile.setOnAction(e -> showProfile());

        btnSaveProfile.setOnAction(e -> saveProfile());
        btnDeleteProfile.setOnAction(e -> deleteProfile());
        btnExportCSV.setOnAction(e -> exportCSV());
        btnSubmitComplaint.setOnAction(e -> submitComplaint());

        btnExit.setOnAction(e -> Platform.exit());
        btnLogout.setOnAction(e -> logout());
    }

    private void setupHistoryFilter() {
        cbHistoryFilter.setItems(FXCollections.observableArrayList("All", "Today", "Last 2 Days", "This Week"));
        cbHistoryFilter.getSelectionModel().selectFirst();

        cbHistoryFilter.valueProperty().addListener((obs, oldV, newV) -> refreshHistoryCards());
    }
    private void refreshHistoryCards() {
        loadHistoryOrders();

        String filter = cbHistoryFilter.getValue();
        LocalDate today = LocalDate.now();

        filteredHistoryOrders.setPredicate(o -> {
            LocalDate orderDate = o.getDate();

            return switch (filter) {
                case "Today" ->
                        orderDate.isEqual(today);

                case "Last 2 Days" ->
                        !orderDate.isBefore(today.minusDays(2));

                case "This Week" -> {
                    LocalDate startOfWeek =
                            today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                    yield !orderDate.isBefore(startOfWeek) && !orderDate.isAfter(today);
                }

                default -> true; // All
            };
        });
    }

    // ====================== ORDERS SECTION ======================
    private void setupOrdersSection() {
        deliveryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDonorAddress.setCellValueFactory(new PropertyValueFactory<>("donorAddress"));
        colDonorPhone.setCellValueFactory(new PropertyValueFactory<>("donorPhone"));
        colCharityAddress.setCellValueFactory(new PropertyValueFactory<>("charityAddress"));
        colCharityPhone.setCellValueFactory(new PropertyValueFactory<>("charityPhone"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("Date"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status color
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null); setStyle("");
                } else {
                    setText(status);
                    switch (status.toLowerCase()) {
                        case "pending" -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        case "completed" -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        // Action column
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Complete");
            {
                btn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    Order order = getTableRow().getItem();
                    if (order != null && !"Completed".equals(order.getStatus())) {
                        order.setStatus("Completed");
                        FileManager.updateOrderStatus(order.getId(), "Completed", deliveryUser.getUserName());
                        getTableView().refresh();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Order order = getTableRow().getItem();
                btn.setDisable("Completed".equals(order.getStatus()));
                setGraphic(btn);
            }
        });

        // Filter & search
        filteredOrders = new FilteredList<>(orders, o -> true);
        deliveryTable.setItems(filteredOrders);
        txtSearch.textProperty().addListener((obs, oldV, newV) -> filterOrders());
        cbStatusFilter.getItems().addAll("All", "Pending", "Completed");
        cbStatusFilter.getSelectionModel().selectFirst();
        cbStatusFilter.valueProperty().addListener((obs, oldV, newV) -> filterOrders());
    }

    private void loadOrders() {
        orders.clear();
        Map<String, User> users = FileManager.loadUsers();
        List<Order> loaded = FileManager.loadOrders(users);
        orders.addAll(loaded);
    }

    private void filterOrders() {
        filteredOrders.setPredicate(o -> {
            String searchText = txtSearch.getText() != null ? txtSearch.getText().toLowerCase() : "";
            String orderStatus = o.getStatus() != null ? o.getStatus() : "";

            boolean matchSearch = (o.getCharityAddress() != null && o.getCharityAddress().toLowerCase().contains(searchText)) ||
                                  (o.getDonorAddress() != null && o.getDonorAddress().toLowerCase().contains(searchText));

            boolean matchStatus = "All".equals(cbStatusFilter.getValue()) || cbStatusFilter.getValue().equals(orderStatus);

            return matchSearch && matchStatus;
        });
    }



    private void showOrdersSection() {
        ordersSection.setVisible(true);
        historySection.setVisible(false);
        complaintsSection.setVisible(false);
        profileSection.setVisible(false);
    }

    // ====================== HISTORY SECTION ======================
    private void setupHistorySection() {
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colHistoryId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHistoryDonorAddress.setCellValueFactory(new PropertyValueFactory<>("donorAddress"));
        colHistoryDonorPhone.setCellValueFactory(new PropertyValueFactory<>("donorPhone"));
        colHistoryCharityAddress.setCellValueFactory(new PropertyValueFactory<>("charityAddress"));
        colHistoryCharityPhone.setCellValueFactory(new PropertyValueFactory<>("charityPhone"));
        colHistoryDate.setCellValueFactory(new PropertyValueFactory<>("Date"));
        colHistoryStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredHistoryOrders = new FilteredList<>(historyOrders, o -> true);
        historyTable.setItems(filteredHistoryOrders);

        txtHistorySearch.textProperty().addListener((obs, oldV, newV) -> filterHistoryOrders());
        cbHistoryFilter.valueProperty().addListener((obs, oldV, newV) -> filterHistoryOrders());

        cbHistoryFilter.setItems(FXCollections.observableArrayList("All", "Today", "Last 2 Days", "This Week"));
        cbHistoryFilter.getSelectionModel().selectFirst();
    }

    private void loadHistoryOrders() {
        historyOrders.clear();
        Map<String, User> users = FileManager.loadUsers();
        List<Order> allOrders = FileManager.loadOrders(users);

        for (Order o : allOrders) {
            String deliveryName = o.getDeliveryUserName(); 
            String status = o.getStatus();

            if (deliveryUser.getUserName().equals(deliveryName) &&
                    "Completed".equalsIgnoreCase(status)) {
                historyOrders.add(o);
            }
        }
    }

    private void filterHistoryOrders() {
        String searchText = txtHistorySearch.getText() != null ? txtHistorySearch.getText().toLowerCase() : "";
        String filter = cbHistoryFilter.getValue() != null ? cbHistoryFilter.getValue() : "All";

        LocalDate today = LocalDate.now();

        filteredHistoryOrders.setPredicate(o -> {
            boolean matchSearch = (o.getCharityAddress() != null && o.getCharityAddress().toLowerCase().contains(searchText)) ||
                                  (o.getDonorAddress() != null && o.getDonorAddress().toLowerCase().contains(searchText));

            boolean matchStatus = switch (filter) {
                case "Today" -> o.getDate().isEqual(today);
                case "Last 2 Days" -> !o.getDate().isBefore(today.minusDays(2));
                case "This Week" -> {
                    LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                    yield !o.getDate().isBefore(startOfWeek) && !o.getDate().isAfter(today);
                }
                default -> true; 
            };

            return matchSearch && matchStatus;
        });
    }

    private void showHistorySection() {
        historySection.setVisible(true);
        ordersSection.setVisible(false);
        complaintsSection.setVisible(false);
        profileSection.setVisible(false);

        loadHistoryOrders();  
        filterHistoryOrders();
    }

    // ====================== PROFILE SECTION ======================
    private void setupProfileSection() {
        // Nothing special; fields prefilled in showProfile
    }

    private void showProfile() {
        profileSection.setVisible(true);
        ordersSection.setVisible(false);
        historySection.setVisible(false);
        complaintsSection.setVisible(false);

        txtProfileName.setText(deliveryUser.getName());
        txtProfilePhone.setText(deliveryUser.getPhone());
        txtProfilePassword.setText(deliveryUser.getPassword());
        txtProfileAddress.setText(deliveryUser.getAddress());
    }
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

    private void saveProfile() {
        UserManager um = UserManager.getInstance();

        String newName = txtProfileName.getText().trim();

        String newPhone = txtProfilePhone.getText().trim();
        String newPassword = txtProfilePassword.getText();
        String newAddress = txtProfileAddress.getText().trim();

        // ----------- VALIDATION -----------

        // 1. No field should be empty
        if (newName.isEmpty() || newPhone.isEmpty()
                || newPassword.isEmpty() || newAddress.isEmpty()) {

            showStyledAlert("All fields are required!", true);
            return;
        }


        // 3. Password must be at least 4 characters
        if (newPassword.length() < 4) {
            showStyledAlert("Password must be at least 4 characters!", true);
            return;
        }

        // 4. Phone must be exactly 11 digits
        if (!newPhone.matches("\\d{11}")) {
            showStyledAlert("Phone must be 11 digits!", true);
            return;
        }

        deliveryUser.setName(txtProfileName.getText().trim());
        deliveryUser.setPhone(txtProfilePhone.getText().trim());
        deliveryUser.setPassword(txtProfilePassword.getText());
        deliveryUser.setAddress(txtProfileAddress.getText().trim());

        FileManager.saveUsers(um.getAllUsers());

        showStyledAlert("Profile saved successfully!", false); // white for success
    }

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
                UserManager.getInstance().deleteUser(deliveryUser);
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


    // ====================== COMPLAINTS ======================
    private void setupComplaintsSection() {
        // btnSubmitComplaint already setup in setupButtons()
    }

    private void showComplaintsSection() {
        complaintsSection.setVisible(true);
        ordersSection.setVisible(false);
        historySection.setVisible(false);
        profileSection.setVisible(false);
    }

    private void submitComplaint() {
        String desc = txtComplaintDesc.getText().trim();
        if (desc.isEmpty()) { showAlert("Complaint cannot be empty!"); return; }

        Complaint complaint = new Complaint(
                String.valueOf(System.currentTimeMillis()),
                deliveryUser.getClass().getSimpleName(),
                deliveryUser.getUserName(),
                desc,
                LocalDate.now(),
                "Pending"
        );

        FileManager.saveComplaint(complaint);
        txtComplaintDesc.clear();
        showAlert("Complaint submitted successfully!");
    }

    // ====================== UTILS ======================
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtProfileName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Logout failed");
        }
    }

    private void exportCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Deliveries to CSV");
        File f = fc.showSaveDialog(deliveryTable.getScene().getWindow());
        if (f != null) {
            ReportGenerator.exportOrdersCsv(orders, f.getAbsolutePath(), null);
        }
    }

    private void showAlert(String message) {
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
}