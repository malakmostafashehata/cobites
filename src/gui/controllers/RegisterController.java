package gui.controllers;

import backend.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RegisterController {

    @FXML private Label loginHereLabel;
    @FXML private TextField usernameField, nameField, addressField, phoneField, charityCodeField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox, volunteerTypeComboBox;
    @FXML private HBox volunteerTypeBox, charityCodeBox;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;

    private final UserManager userManager = UserManager.getInstance();

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            roleComboBox.getItems().addAll("VOLUNTEER", "DELIVERY", "CHARITY");
            volunteerTypeComboBox.getItems().addAll("Person", "Restaurant", "Hotel");

            volunteerTypeBox.setVisible(false);
            charityCodeBox.setVisible(false);

            loginHereLabel.setStyle("-fx-text-fill: #054561; -fx-cursor: hand;");
            loginHereLabel.setOnMouseClicked(e -> goBackToLogin());

            // إظهار الحقول حسب الدور
            roleComboBox.setOnAction(e -> {
                String role = roleComboBox.getValue();
                volunteerTypeBox.setVisible("VOLUNTEER".equals(role));
                charityCodeBox.setVisible("CHARITY".equals(role));
            });

            registerButton.setOnAction(e -> handleRegister());
        });
    }
    private void goBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/views/Login.fxml"));
            Stage stage = (Stage) loginHereLabel.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Failed to load login screen!");
        }
    }


    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String roleStr = roleComboBox.getValue();
        String type = volunteerTypeComboBox.getValue();
        String code = charityCodeField.getText().trim();

        statusLabel.setStyle("-fx-text-fill: red;");

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() ||
            address.isEmpty() || phone.isEmpty() || roleStr == null ||
            (roleStr.equals("CHARITY") && code.isEmpty())) {
            statusLabel.setText("Please fill all fields!");
            return;
        }
        // Charity code must be exactly 7 digits
        if ("CHARITY".equals(roleStr) && !code.matches("\\d{7}")) {
            statusLabel.setText("Charity code must be exactly 7 digits!");
            return;
        }


        if (userManager.usernameExists(username)) {
            statusLabel.setText("Username already exists!");
            return;
        }

        if (password.length() < 4) {
            statusLabel.setText("Password must be at least 4 characters!");
            return;
        }

        if (!phone.matches("\\d{11}")) {
            statusLabel.setText("Phone number must be exactly 11 digits!");
            return;
        }

        User newUser = null;
        Role role = Role.valueOf(roleStr);

        try {
            switch (role) {
                case VOLUNTEER -> {
                    if (type == null) {
                        statusLabel.setText("Please select volunteer type!");
                        return;
                    }
                    newUser = switch (type) {
                        case "Person" -> new PersonVolunteer(username, password, name, address, phone);
                        case "Restaurant" -> new RestaurantVolunteer(username, password, name, address, "Restaurant", phone);
                        case "Hotel" -> new HotelVolunteer(username, password, name, address, "Hotel", phone);
                        default -> throw new IllegalArgumentException("Invalid volunteer type");
                    };
                }
                case DELIVERY -> newUser = new DeliveryPerson(username, password, name, address, phone);
                case CHARITY -> {
                    Charity charity = new Charity(username, password, name, address, phone, code);
                    charity.setReviewStatus("append"); 
                    newUser = charity;

                    userManager.addUser(newUser);
                    FileManager.saveUsers(userManager.getAllUsers());

                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Thank you for registering! we will contact you soon .");
                    clearFields();
                    return; 
                }
            }

            userManager.addUser(newUser);
            FileManager.saveUsers(userManager.getAllUsers());

            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("User registered successfully!");
            clearFields();

            openDashboard(newUser);

        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Error during registration!");
        }
    }
    private void openDashboard(User user) {
        try {
            FXMLLoader loader;
            Parent root;
            Stage stage = (Stage) registerButton.getScene().getWindow();

            // تحميل الـ FXML
            if (user instanceof Volunteer v) {
                loader = new FXMLLoader(getClass().getResource("/gui/views/volunteerDashboard.fxml"));
                root = loader.load();
                ((VolunteerDashboardController) loader.getController()).initData(v);

            } else if (user instanceof DeliveryPerson d) {
                loader = new FXMLLoader(getClass().getResource("/gui/views/deliveryDashboard.fxml"));
                root = loader.load();
                ((DeliveryDashboardController) loader.getController()).initData(d);

            } else {
                return;
            }

            root.setOpacity(0);    

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();

            // ====== Fade In Animation ======
            FadeTransition fade = new FadeTransition(Duration.millis(300), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load dashboard!");
        }
    }



    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        nameField.clear();
        addressField.clear();
        phoneField.clear();
        charityCodeField.clear();
        roleComboBox.setValue(null);
        volunteerTypeComboBox.setValue(null);
        volunteerTypeBox.setVisible(false);
        charityCodeBox.setVisible(false);
    }
}