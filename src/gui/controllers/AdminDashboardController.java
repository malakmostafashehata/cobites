package gui.controllers;
import backend.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.util.Duration;

public class AdminDashboardController {

    // ===================== EXPORT BUTTONS =====================
    @FXML private Button btnExportUsers, btnExportDonations, btnExportOrders, btnExportStock, btnExportComplaints, btnExportcharity;

    // ===================== SECTIONS =====================
    @FXML private VBox usersSection, donationsSection, ordersSection, stockSection, complaintsSection, charitysSection;
    @FXML private ComboBox<String> cbComplaintRoleFilter;

    // ===================== SIDE NAV BUTTONS =====================
    @FXML private Button btnUsers, btnDonations, btnOrders, btnStock, btnComplaints, btnCharity, btnProfileTab;

    // ===================== TABLES =====================
    @FXML private TableView<List<String>> usersTable, donationsTable, ordersTable, stockTable, complaintsTable, charityTable;

    // ===================== SEARCH FIELDS =====================
    @FXML private TextField txtSearchUsers, txtSearchDonations, txtSearchOrders, txtSearchStock, txtSearchComplaints;
    @FXML private DatePicker CdatePicker;

    // ===================== TOP BAR =====================
    @FXML private Label adminNameLabel;
    @FXML private Button btnTopLogout, btnExit;
    @FXML private ComboBox<String> cbUserRoleFilter;

    // ===================== ADD/DELETE BUTTONS =====================
    @FXML private Button btnAddUser, btnDeleteUser;
    @FXML private Button btnDeleteDonation;
    @FXML private Button btnDeleteOrder;
    @FXML private Button btnDeleteComplaint;
    @FXML private Button btnDeleteCharity;


    // ===================== DATA =====================
    private Admin admin;
    private Map<String, User> allUsers;
    private ObservableList<List<String>> usersData, donationsData, ordersData, stockData, complaintsData;
    @FXML private ComboBox<String> cbDonationType;
    @FXML private ComboBox<String> cbOrderStatus;
    @FXML private DatePicker OdatePicker;
    @FXML private ComboBox<String> cbStockQty;
    @FXML private DatePicker DdatePicker;
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");


    // ===================== INITIALIZE =====================
    @FXML
    public void initialize() {
        // Setup table columns dynamically
        setupColumns(usersTable, List.of("Role","Username","Name","Phone","Address"));
        setupColumns(donationsTable, List.of("ID","Name","quantity","Date","Donor Name","Donor Address","Donor Phone","Type"));
        setupColumns(ordersTable, List.of("ID","Charity","Item","quantity","Date","Charity Address","Donor Address","Charity Phone","Donor Phone","Status","delivery name "));
        setupColumns(stockTable, List.of("ID","Item","quantity","Type"));
        setupColumns(complaintsTable, List.of("ID","Charity","Description","Date"));
        setupColumns(charityTable, List.of("Role","Username","Name","Phone","Address"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DdatePicker.setPromptText(LocalDate.now().format(formatter));
        OdatePicker.setPromptText(LocalDate.now().format(formatter));
        CdatePicker.setPromptText(LocalDate.now().format(formatter));






    }
    private void filterStock() {
        ObservableList<List<String>> filtered = FXCollections.observableArrayList();

        for (List<String> row : stockData) {
            String qty = row.get(2);

            boolean match =
                    cbStockQty.getValue().equals("All") ||
                            (cbStockQty.getValue().equals("Available") && !qty.equals("All items donated")) ||
                            (cbStockQty.getValue().equals("All Donated") && qty.equals("All items donated"));

            if (match) filtered.add(row);
        }
        stockTable.setItems(filtered);
    }
    private ObservableList<List<String>> charityData;

    private void loadCharities() {
        charityData = FXCollections.observableArrayList();

        allUsers.values().forEach(user -> {
            if (user instanceof Charity c) {
                charityData.add(new ArrayList<>(List.of(
                        "Charity",
                        c.getUserName(),
                        c.getName(),
                        c.getPhone(),
                        c.getAddress(),
                        c.getCode(),
                        c.getReviewStatus()
                )));
            }
        });

        setupCharityColumns();
        charityTable.setItems(charityData);
    }

    private void filterOrders() {
        ObservableList<List<String>> filtered = FXCollections.observableArrayList();

        for (List<String> row : ordersData) {
            boolean matchStatus =
                    cbOrderStatus.getValue().equals("All") ||
                            row.get(9).equalsIgnoreCase(cbOrderStatus.getValue());

            boolean matchDate =
                    OdatePicker.getValue() == null ||
                            row.get(4).contains(OdatePicker.getValue().toString());

            if (matchStatus && matchDate) {
                filtered.add(row);
            }
        }
        ordersTable.setItems(filtered);
    }

    private void filterDonations() {
        ObservableList<List<String>> filtered = FXCollections.observableArrayList();

        for (List<String> row : donationsData) {
            boolean matchType =
                    cbDonationType.getValue().equals("All") ||
                            row.get(7).equalsIgnoreCase(cbDonationType.getValue());

            boolean matchDate =
                    DdatePicker.getValue() == null ||
                            row.get(3).contains(DdatePicker.getValue().toString());

            if (matchType && matchDate) {
                filtered.add(row);
            }
        }
        donationsTable.setItems(filtered);
    }

    private void filterUsersByRole() {
        String role = cbUserRoleFilter.getValue();

        if (role.equals("All")) {
            usersTable.setItems(usersData);
            return;
        }

        ObservableList<List<String>> filtered = FXCollections.observableArrayList();
        for (List<String> row : usersData) {
            if (row.get(0).equalsIgnoreCase(role)) {
                filtered.add(row);
            }
        }
        usersTable.setItems(filtered);
    }
    private void filterComplaints() {
        if (complaintsData == null) return;
        String keyword = txtSearchComplaints.getText() != null ? txtSearchComplaints.getText().toLowerCase() : "";
        ObservableList<List<String>> filtered = FXCollections.observableArrayList();

        for (List<String> row : complaintsData) {
            boolean matchKeyword = keyword.isEmpty();
            for (String cell : row) {
                if (cell != null && cell.toLowerCase().contains(keyword)) {
                    matchKeyword = true;
                    break;
                }
            }

            boolean matchDate = CdatePicker.getValue() == null || row.get(3).contains(CdatePicker.getValue().toString());

            if (matchKeyword && matchDate) filtered.add(row);
        }

        complaintsTable.setItems(filtered);
    }
    private void filterComplaintsByRole() {
        if (filteredComplaints == null) return;

        String selectedRole = cbComplaintRoleFilter.getValue();

        filteredComplaints.setPredicate(row -> {
            if (selectedRole == null || selectedRole.equals("All"))
                return true;

            String role = row.size() > 4 ? row.get(4) : "";
            return role.equalsIgnoreCase(selectedRole);
        });
    }
    private FilteredList<List<String>> filteredComplaints;
    private void setupCharityColumns() {
        charityTable.getColumns().clear();

        String[] headers = {
                "Role", "Username", "Name", "Phone", "Address", "Code", "Status", "Action"
        };

        for (int i = 0; i < headers.length; i++) {
            final int index = i;

            if (headers[i].equals("Action")) {
                TableColumn<List<String>, Void> actionCol = new TableColumn<>("Action");

                actionCol.setCellFactory(col -> new TableCell<>() {
                    private final Button btn = new Button("Accept");

                    {
                        btn.getStyleClass().add("btn-reply");
                        btn.setOnAction(e -> {
                            List<String> row = getTableView().getItems().get(getIndex());

                            if (row.get(6).equalsIgnoreCase("accepted")) {
                                btn.setDisable(true);
                                return;
                            }

                            row.set(6, "accepted");
                            charityTable.refresh();

                            acceptCharity(row.get(1)); // username
                        });

                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            List<String> row = getTableView().getItems().get(getIndex());
                            if (row.get(6).equalsIgnoreCase("accepted")) {
                                btn.setDisable(true);
                            } else {
                                btn.setDisable(false);
                            }
                            setGraphic(btn);
                        }
                    }

                });

                charityTable.getColumns().add(actionCol);
            } else {
                TableColumn<List<String>, String> col = new TableColumn<>(headers[i]);
                col.setCellValueFactory(cell ->
                        new SimpleStringProperty(
                                index < cell.getValue().size() ? cell.getValue().get(index) : ""
                        )
                );
                charityTable.getColumns().add(col);
            }
        }

        charityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    private void acceptCharity(String username) {

        UserManager um = UserManager.getInstance();
        User u = um.getUserByUsername(username);

        if (u instanceof Charity charity) {
            charity.setReviewStatus("accepted"); 
            um.saveNow();                        
        }
    }



    // ===================== INIT DATA =====================
    public void initData(Admin admin) {
        this.admin = admin;
        txtSearchComplaints.textProperty().addListener((obs, oldV, newV) -> filterComplaints());
        cbComplaintRoleFilter.getItems().addAll("All","Charity","Volunteer","DeliveryPerson");
        cbComplaintRoleFilter.getSelectionModel().selectFirst();

        cbComplaintRoleFilter.valueProperty().addListener((obs, oldV, newV) -> filterComplaintsByRole());

        // Load users from storage
        allUsers = FileManager.loadUsers();
        CdatePicker.valueProperty().addListener((o, a, b) -> filterComplaints());
        initComplaintStatusFilter();

        // Load all tables
        loadUsers();
        loadDonations();
        loadOrders();
        loadStock();
        loadComplaints();
        initComplaintStatusFilter();
        loadCharities();
        btnCharity.setOnAction(e -> showSection(charitysSection));

        // Side navigation
        btnUsers.setOnAction(e -> showSection(usersSection));
        btnDonations.setOnAction(e -> showSection(donationsSection));
        btnOrders.setOnAction(e -> showSection(ordersSection));
        btnStock.setOnAction(e -> showSection(stockSection));
        btnComplaints.setOnAction(e -> showSection(complaintsSection));
        btnCharity.setOnAction(e -> showSection(charitysSection));

        // Top bar
        btnTopLogout.setOnAction(e -> logout());
        btnExit.setOnAction(e -> Platform.exit());

        // User management
        btnAddUser.setOnAction(e -> addUser());
        btnDeleteUser.setOnAction(e -> deleteSelectedUser());
        btnDeleteDonation.setOnAction(e -> deleteSelectedDonation());
        btnDeleteOrder.setOnAction(e -> deleteSelectedOrder());
        btnDeleteComplaint.setOnAction(e -> deleteSelectedComplaint());
        btnDeleteCharity.setOnAction(e -> deleteSelectedCharity());



        // Export buttons
        btnExportUsers.setOnAction(e ->
                exportTableCsv(usersTable, "Users_Report")
        );
        btnExportDonations.setOnAction(e ->
                exportTableCsv(donationsTable, "Donations_Report")
        );
        btnExportOrders.setOnAction(e ->
                exportTableCsv(ordersTable, "Orders_Report")
        );
        btnExportStock.setOnAction(e ->
                exportTableCsv(stockTable, "Stock_Report")
        );
        btnExportComplaints.setOnAction(e ->
                exportTableCsv(complaintsTable, "Complaints_Report")
        );
        btnExportcharity.setOnAction(e ->
                exportTableCsv(charityTable, "Charity_Report")
        );

        cbUserRoleFilter.getItems().addAll("All","Charity","PersonVolunteer","HotelVolunteer","RestaurantVolunteer","DeliveryPerson");
        cbUserRoleFilter.getSelectionModel().selectFirst();

        cbUserRoleFilter.valueProperty().addListener((obs,o,n) ->
                filterUsersByRole()
        );
        cbDonationType.getItems().addAll("All","Meal","Dessert","Drink");
        cbDonationType.getSelectionModel().selectFirst();

        cbDonationType.valueProperty().addListener((o,a,b)->filterDonations());
        DdatePicker.valueProperty().addListener((o,a,b)->filterDonations());
        cbOrderStatus.getItems().addAll("All","Pending","Completed");
        cbOrderStatus.getSelectionModel().selectFirst();

        cbOrderStatus.valueProperty().addListener((o,a,b)->filterOrders());
        OdatePicker.valueProperty().addListener((o,a,b)->filterOrders());
        cbStockQty.getItems().addAll("All","Available","All Donated");
        cbStockQty.getSelectionModel().selectFirst();

        cbStockQty.valueProperty().addListener((o,a,b)->filterStock());


        // ===================== SEARCH / FILTER =====================
        txtSearchUsers.textProperty().addListener((obs, oldV, newV) -> filterTable(usersTable, usersData, newV));
        txtSearchDonations.textProperty().addListener((obs, oldV, newV) -> filterTable(donationsTable, donationsData, newV));
        txtSearchOrders.textProperty().addListener((obs, oldV, newV) -> filterTable(ordersTable, ordersData, newV));
        txtSearchStock.textProperty().addListener((obs, oldV, newV) -> filterTable(stockTable, stockData, newV));
        txtSearchComplaints.textProperty().addListener((obs, oldV, newV) -> filterTable(complaintsTable, complaintsData, newV));

        showSection(usersSection); // default visible section
    }

    // ===================== SECTION SWITCH =====================
    private void showSection(VBox target) {
        usersSection.setVisible(false);
        donationsSection.setVisible(false);
        ordersSection.setVisible(false);
        stockSection.setVisible(false);
        complaintsSection.setVisible(false);
        charitysSection.setVisible(false);
        target.setVisible(true);
    }


    // ===================== LOAD TABLE DATA =====================
    private void loadUsers() {
        usersData = FXCollections.observableArrayList();
        for(User u : allUsers.values()) {
            if (u instanceof Admin) continue;

            String phone = "", address = "";
            if (u instanceof Volunteer v) { phone = v.getPhone(); address = v.getAddress(); }
            else if (u instanceof Charity c) { phone = c.getPhone(); address = c.getAddress(); }
            else if (u instanceof DeliveryPerson d) { phone = d.getPhone(); address = d.getAddress(); }

            usersData.add(List.of(
                    u.getClass().getSimpleName(),
                    u.getUserName(),
                    u.getName(),
                    phone != null ? phone : "",
                    address != null ? address : ""
            ));
        }
        usersTable.setItems(usersData);
    }

    private void loadDonations() {
        donationsData = FXCollections.observableArrayList();
        List<Donation> donations = FileManager.loadDonations(allUsers, null);
        for (Donation d : donations) {
            FoodItem item = d.getItem();
            String donorName = d.getDonor() != null ? d.getDonor().getUserName() : "Unknown";

            donationsData.add(List.of(
                    d.getId(),
                    item.getName(),
                    String.valueOf(item.getQty()),
                    d.getDate().toString(),
                    donorName,
                    item.getDonorAddress() != null ? item.getDonorAddress() : "",
                    item.getDonorPhone() != null ? item.getDonorPhone() : "",
                    item.getType().name()
            ));
        }
        donationsTable.setItems(donationsData);
    }

    private void loadOrders() {
        ordersData = FXCollections.observableArrayList();
        List<Order> orders = FileManager.loadOrders(allUsers);
        for (Order o : orders) {
            ordersData.add(List.of(
                    o.getId(),
                    o.getCharityUsername(),
                    o.getItemName(),
                    String.valueOf(o.getQuantity()),
                    o.getDate().toString(),
                    o.getCharityAddress(),
                    o.getDonorAddress(),
                    o.getCharityPhone(),
                    o.getDonorPhone(),
                    o.getStatus(),
                    o.getDeliveryUserName()
            ));
        }
        ordersTable.setItems(ordersData);
    }

    private void loadStock() {
        stockData = FXCollections.observableArrayList();
        List<Donation> stock = FileManager.loadCharityStack(allUsers);

        for (Donation d : stock) {
            String qtyText = d.getItem().getQty() > 0
                    ? String.valueOf(d.getItem().getQty())
                    : "All items donated"; 

            stockData.add(List.of(
                    d.getId(),
                    d.getItem().getName(),
                    qtyText,
                    d.getItem().getType().name()
            ));
        }
        stockTable.setItems(stockData);
    }


    private void loadComplaints() {
        complaintsData = FXCollections.observableArrayList();
        List<Complaint> complaints = FileManager.loadComplaints();

        for (Complaint c : complaints) {
            ArrayList<String> row = new ArrayList<>(List.of(
                    c.getId(),
                    c.getUsername(),
                    c.getDescription(),
                    c.getDate().toString(),
                    c.getRole(),
                    c.getStatus() != null ? c.getStatus() : "Pending"
            ));

            complaintsData.add(row);
        }
        filteredComplaints = new FilteredList<>(complaintsData, p -> true);
        complaintsTable.setItems(filteredComplaints);


        setupComplaintsColumns();
    }

    private void setupComplaintsColumns() {
        complaintsTable.getColumns().clear();

        String[] headers = {"ID", "Username", "Description", "Date", "Role", "Status", "Action"};

        for (int i = 0; i < headers.length; i++) {
            final int index = i;

            if (headers[i].equals("Action")) {
                TableColumn<List<String>, Void> actionCol = new TableColumn<>(headers[i]);

                actionCol.setCellFactory(col -> new TableCell<>() {
                    private final Button btn = new Button("Reply");

                    {
                        btn.getStyleClass().add("btn-reply");
                        btn.setOnAction(e -> {
                            List<String> row = getTableView().getItems().get(getIndex());
                            String username = row.get(1);

                            openEmailClient(username);

                            row.set(5, "Answered");
                            complaintsTable.refresh();

                            updateComplaintStatus(row.get(0), "Answered"); 
                        });

                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                });

                complaintsTable.getColumns().add(actionCol);
            } else {
                TableColumn<List<String>, String> col = new TableColumn<>(headers[i]);
                col.setCellValueFactory(cell ->
                        new SimpleStringProperty(index < cell.getValue().size() ? cell.getValue().get(index) : "")
                );
                complaintsTable.getColumns().add(col);
            }
        }

        complaintsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    private void updateComplaintStatus(String id, String newStatus) {
        List<Complaint> complaints = FileManager.loadComplaints();
        for (Complaint c : complaints) {
            if (c.getId().equals(id)) {
                c.setStatus(newStatus);
                break;
            }
        }
        ObservableList<List<String>> complaintsRows = complaintsTable.getItems();
        FileManager.saveAllComplaints(complaintsRows);
    }

    private void openEmailClient(String username) {
        try {
            String email = username;
            Desktop.getDesktop().mail(new URI("mailto:" + email + "?subject=Reply%20to%20your%20complaint"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ===================== LOGOUT =====================
    
    private void logout() {
        try {
            Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/gui/views/Login.fxml"));
            
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Login");
            stage.setResizable(false);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML private ComboBox<String> cbComplaintStatus;

    private void initComplaintStatusFilter() {
        if (cbComplaintStatus == null) return;

        cbComplaintStatus.getItems().setAll("All", "Pending", "Answered");
        cbComplaintStatus.getSelectionModel().selectFirst();

        cbComplaintStatus.valueProperty()
                .addListener((obs, oldV, newV) -> filterComplaintsByStatus());
    }

    private void filterComplaintsByStatus() {
        if (filteredComplaints == null) return;

        String status = cbComplaintStatus.getValue();
        String roleFilter = cbComplaintRoleFilter.getValue();

        filteredComplaints.setPredicate(row -> {

            // ===== ROLE FILTER =====
            boolean roleMatch =
                    roleFilter == null ||
                            roleFilter.equals("All") ||
                            (row.size() > 4 && row.get(4) != null &&
                                    row.get(4).equalsIgnoreCase(roleFilter));

            // ===== STATUS FILTER =====
            boolean statusMatch =
                    status == null ||
                            status.equals("All") ||
                            (row.size() > 5 && row.get(5) != null &&
                                    row.get(5).equalsIgnoreCase(status));

            return roleMatch && statusMatch;
        });
    }

    // =====================================================
    // =============== ADD / DELETE HANDLERS ==============
    // =====================================================
    private void addUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.setHeaderText(null);

        // LOAD CSS
        URL cssURL = getClass().getResource("/gui/views/dashboard.css");
        if (cssURL != null) {
            dialog.getDialogPane().getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.out.println("⚠ CSS NOT FOUND at /gui/views/dashboard.css");
        }

        dialog.getDialogPane().getStyleClass().add("donation-form");

        // ================= TITLE =================
        Label title = new Label("Add New User");
        title.getStyleClass().add("form-title");

        // ================= GRID ==================
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.getStyleClass().add("form-grid");

        // --------- Fields ---------
        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Username");
        txtUsername.getStyleClass().add("input-field");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Password");
        txtPassword.getStyleClass().add("input-field");

        TextField txtName = new TextField();
        txtName.setPromptText("Name");
        txtName.getStyleClass().add("input-field");

        TextField txtPhone = new TextField();
        txtPhone.setPromptText("Phone");
        txtPhone.getStyleClass().add("input-field");

        TextField txtAddress = new TextField();
        txtAddress.setPromptText("Address");
        txtAddress.getStyleClass().add("input-field");

        ComboBox<String> cmbRole = new ComboBox<>();
        cmbRole.getItems().addAll("Charity","PersonVolunteer","RestaurantVolunteer","HotelVolunteer","Delivery");
        cmbRole.getSelectionModel().selectFirst();
        cmbRole.getStyleClass().add("combo-box");

        // Status label for validation messages
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); // RED & BOLD
        statusLabel.setWrapText(true); // allow multi-line errors
        statusLabel.setMaxWidth(250);

        // --------- Layout in Grid ---------
        grid.add(new Label("Role:"), 0, 0);
        grid.add(cmbRole, 1, 0);

        grid.add(new Label("Username:"), 0, 1);
        grid.add(txtUsername, 1, 1);

        grid.add(new Label("Password:"), 0, 2);
        grid.add(txtPassword, 1, 2);

        grid.add(new Label("Name:"), 0, 3);
        grid.add(txtName, 1, 3);

        grid.add(new Label("Phone:"), 0, 4);
        grid.add(txtPhone, 1, 4);

        grid.add(new Label("Address:"), 0, 5);
        grid.add(txtAddress, 1, 5);

        grid.getChildren().forEach(n -> {
            if (n instanceof Label l) l.getStyleClass().add("form-label");
        });

        // ============== BUTTONS ===============
        ButtonType btnAddType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAddType, ButtonType.CANCEL);

        Button btnAdd = (Button) dialog.getDialogPane().lookupButton(btnAddType);
        btnAdd.getStyleClass().add("btn-action");

        Button btnCancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        btnCancel.getStyleClass().add("btn-action");

        // ============== CONTENT ===============
        VBox layout = new VBox(10.0, title, grid, statusLabel);
        layout.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(layout);

        // ===== Validation BEFORE closing dialog =====
        btnAdd.addEventFilter(ActionEvent.ACTION, event -> {
            String role = cmbRole.getValue();
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String address = txtAddress.getText().trim();

            // ---- EMPTY FIELD CHECK ----
            if (username.isEmpty() || password.isEmpty() || name.isEmpty() ||
                    address.isEmpty() || phone.isEmpty() || role == null) {
                statusLabel.setText("Please fill all fields!\nAll fields are required."); // two lines
                event.consume(); // Prevent dialog from closing
                return;
            }

            // ---- UNIQUE USERNAME ----
            if (allUsers.containsKey(username)) {
                statusLabel.setText("Username already exists!\nChoose a different one."); // two lines
                event.consume();
                return;
            }

            // ---- PASSWORD LENGTH ----
            if (password.length() < 4) {
                statusLabel.setText("Password must be at least 4 characters!\nIncrease the length."); // two lines
                event.consume();
                return;
            }

            // ---- PHONE FORMAT ----
            if (!phone.matches("\\d{11}")) {
                statusLabel.setText("Phone number must be exactly 11 digits!\nUse numbers only."); // two lines
                event.consume();
            }
        });
        // ===== Convert result =====
        dialog.setResultConverter(btn -> {
            if (btn == btnAddType) {
                String role = cmbRole.getValue();
                String username = txtUsername.getText().trim();
                String password = txtPassword.getText().trim();
                String name = txtName.getText().trim();
                String phone = txtPhone.getText().trim();
                String address = txtAddress.getText().trim();
                return switch (role) {
                    case "Charity" -> new Charity(username, password, name, address, phone, address);
                    case "PersonVolunteer" -> new PersonVolunteer(username, password, name, address, phone);
                    case "RestaurantVolunteer" -> new RestaurantVolunteer(username, password, name, address, "RestaurantName", phone);
                    case "HotelVolunteer" -> new HotelVolunteer(username, password, name, address, "HotelName", phone);
                    case "Delivery" -> new DeliveryPerson(username, password, name, address, phone);
                    default -> null;
                };
            }
            return null;
        });
        // ===== Show dialog =====
        dialog.showAndWait().ifPresent(user -> {
            allUsers.put(user.getUserName(), user);
            FileManager.saveUsers(new ArrayList<>(allUsers.values()));
            loadUsers();
        });}

    private void deleteSelectedUser() {
        List<String> selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String username = selected.get(1);
        // CREATE CONFIRMATION DIALOG
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete User");
        dialog.setHeaderText(null);
        // LOAD CSS
        URL cssURL = getClass().getResource("/gui/views/dashboard.css");
        if (cssURL != null) {
            dialog.getDialogPane().getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.out.println("⚠ CSS NOT FOUND at /gui/views/dashboard.css");
        }
        dialog.getDialogPane().getStyleClass().add("donation-form");
        // MESSAGE
        Label messageLabel = new Label("Are you sure you want to delete user: " + username + " ?");
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(250);
        VBox layout = new VBox(10, messageLabel);
        layout.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(layout);
        // BUTTONS
        ButtonType btnDelete = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(btnDelete, btnCancel);
        // STYLE BUTTONS
        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(btnDelete);
        deleteButton.getStyleClass().add("btn-logout");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(btnCancel);
        cancelButton.getStyleClass().add("btn-action");
        // SHOW DIALOG
        dialog.showAndWait().ifPresent(result -> {
            if (result == btnDelete) {
                allUsers.remove(username);
                FileManager.saveUsers(new ArrayList<>(allUsers.values()));
                loadUsers();
                System.out.println("User deleted: " + username);
            }
        });}
    // ===================== EXPORT TABLE =====================
    private void exportTableCsv(
            TableView<List<String>> table,
            String defaultName
    ) {
        if (table == null || table.getItems().isEmpty()) return;

        FileChooser fc = new FileChooser();
        fc.setInitialFileName(defaultName + ".csv");

        File file = fc.showSaveDialog(table.getScene().getWindow());
        if (file == null) return;

        // ===== Collect Headers =====
        List<String> headers = new ArrayList<>();
        for (TableColumn<?, ?> col : table.getColumns()) {
            headers.add(col.getText());
        }

        // ===== Export =====
        ReportGenerator.exportListCsv(
                table.getItems(),
                headers,
                file.getAbsolutePath()
        );}
    // ===================== DYNAMIC TABLE COLUMNS =====================
    private void setupColumns(TableView<List<String>> table, List<String> headers) {
        if (table == null) return;
        table.getColumns().clear();

        for (int i = 0; i < headers.size(); i++) {
            final int index = i;
            TableColumn<List<String>, String> col = new TableColumn<>(headers.get(i));
            col.setCellValueFactory(cell -> new SimpleStringProperty(
                    index < cell.getValue().size() ? cell.getValue().get(index) : ""
            ));
            table.getColumns().add(col);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    // ===================== FILTER FUNCTION =====================
    private void filterTable(TableView<List<String>> table, ObservableList<List<String>> originalData, String keyword) {
        if (table == null || originalData == null) return;
        if (keyword == null || keyword.isEmpty()) {
            table.setItems(originalData);
            return;
        }
        String lower = keyword.toLowerCase();
        ObservableList<List<String>> filtered = FXCollections.observableArrayList();
        for (List<String> row : originalData) {
            for (String cell : row) {
                if (cell != null && cell.toLowerCase().contains(lower)) {
                    filtered.add(row);
                    break;
                }
            }
        }
        table.setItems(filtered);
    }
    private void deleteSelectedDonation() {
        List<String> selected = donationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String donationId   = selected.get(0);
        String donationName = selected.get(1); // Item Name

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Donation");
        dialog.setHeaderText(null);

        URL cssURL = getClass().getResource("/gui/views/dashboard.css");
        if (cssURL != null)
            dialog.getDialogPane().getStylesheets().add(cssURL.toExternalForm());

        dialog.getDialogPane().getStyleClass().add("donation-form");

        Label messageLabel = new Label(
                "Are you sure you want to delete donation:" +
                        donationName + " ?"
        );
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        VBox layout = new VBox(10, messageLabel);
        layout.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(layout);

        ButtonType btnDelete = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(btnDelete, btnCancel);

        ((Button) dialog.getDialogPane().lookupButton(btnDelete)).getStyleClass().add("btn-logout");
        ((Button) dialog.getDialogPane().lookupButton(btnCancel)).getStyleClass().add("btn-action");

        dialog.showAndWait().ifPresent(result -> {
            if (result == btnDelete) {

                List<Donation> donations = FileManager.loadDonations(allUsers, null);
                donations.removeIf(d -> d.getId().equals(donationId));

                FileManager.rewriteDonations(donations);
                FileManager.updateCharityStack(donations);

                loadDonations();
                loadStock();

                System.out.println("Donation deleted: " + donationName);
            }
        });
    }
    private void deleteSelectedOrder() {
        List<String> selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String orderId   = selected.get(0);
        String orderName = selected.get(2); // Item Name
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Order");
        dialog.setHeaderText(null);
        URL cssURL = getClass().getResource("/gui/views/dashboard.css");
        if (cssURL != null)
            dialog.getDialogPane().getStylesheets().add(cssURL.toExternalForm());

        dialog.getDialogPane().getStyleClass().add("donation-form");

        Label messageLabel = new Label(
                "Are you sure you want to delete order:" +
                        orderName + " ?"
        );
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        VBox layout = new VBox(10, messageLabel);
        layout.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(layout);

        ButtonType btnDelete = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(btnDelete, btnCancel);

        ((Button) dialog.getDialogPane().lookupButton(btnDelete)).getStyleClass().add("btn-logout");
        ((Button) dialog.getDialogPane().lookupButton(btnCancel)).getStyleClass().add("btn-action");

        dialog.showAndWait().ifPresent(result -> {
            if (result == btnDelete) {

                List<Order> orders = FileManager.loadOrders(allUsers);
                for (Order o : orders) {
                    if (o.getId().equals(orderId)) {
                        FileManager.removeOrder(o);
                        break;
                    }
                }
                loadOrders();
                loadStock();

                System.out.println("Order deleted: " + orderName);
            }
        });
    }
    private void deleteSelectedComplaint() {
        List<String> selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Complaint");
        dialog.setHeaderText(null);

        // ===== LOAD CSS =====
        URL cssURL = getClass().getResource("/gui/views/dashboard.css");
        if (cssURL != null)
            dialog.getDialogPane().getStylesheets().add(cssURL.toExternalForm());

        dialog.getDialogPane().getStyleClass().add("donation-form");

        Label messageLabel = new Label(
                "Are you sure you want to delete this complaint?"
        );
        messageLabel.setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;"
        );
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        VBox layout = new VBox(10, messageLabel);
        layout.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(layout);

        ButtonType btnDelete = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(btnDelete, btnCancel);

        ((Button) dialog.getDialogPane().lookupButton(btnDelete))
                .getStyleClass().add("btn-logout");
        ((Button) dialog.getDialogPane().lookupButton(btnCancel))
                .getStyleClass().add("btn-action");

        dialog.showAndWait().ifPresent(result -> {
            if (result == btnDelete) {
                complaintsData.remove(selected);
                FileManager.saveAllComplaints(complaintsTable.getItems());
            }
        });
    }

    private void deleteSelectedCharity() {
        List<String> selected = charityTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String username = selected.get(1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Charity");
        dialog.setHeaderText(null);

        // ===== LOAD CSS =====
        URL cssURL = getClass().getResource("/gui/views/dashboard.css");
        if (cssURL != null)
            dialog.getDialogPane().getStylesheets().add(cssURL.toExternalForm());

        dialog.getDialogPane().getStyleClass().add("donation-form");

        Label messageLabel = new Label(
                "Are you sure you want to delete charity: " + username + " ?"
        );
        messageLabel.setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;"
        );
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        VBox layout = new VBox(10, messageLabel);
        layout.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(layout);

        ButtonType btnDelete = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(btnDelete, btnCancel);

        ((Button) dialog.getDialogPane().lookupButton(btnDelete))
                .getStyleClass().add("btn-logout");
        ((Button) dialog.getDialogPane().lookupButton(btnCancel))
                .getStyleClass().add("btn-action");

        dialog.showAndWait().ifPresent(result -> {
            if (result == btnDelete) {
                allUsers.remove(username);
                FileManager.saveUsers(new ArrayList<>(allUsers.values()));
                charityData.remove(selected);
            }
        });
    }



}