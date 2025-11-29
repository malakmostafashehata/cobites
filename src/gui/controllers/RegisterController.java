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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;    // ← NEW
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> volunteerTypeComboBox;
    @FXML private HBox volunteerTypeBox;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;

    private final UserManager userManager = UserManager.getInstance();

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            roleComboBox.getItems().addAll("VOLUNTEER", "DELIVERY", "CHARITY");
            volunteerTypeComboBox.getItems().addAll("Person", "Restaurant", "Hotel");

            volunteerTypeBox.setVisible(false);

            roleComboBox.setOnAction(e ->
                    volunteerTypeBox.setVisible("VOLUNTEER".equals(roleComboBox.getValue()))
            );

            registerButton.setOnAction(e -> handleRegister());
        });
    }

    private void handleRegister() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String roleStr = roleComboBox.getValue();
        String type = volunteerTypeComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() ||
                address.isEmpty() || phone.isEmpty() || roleStr == null) {

            statusLabel.setText("Please fill all fields!");
            return;
        }

        Role role = Role.valueOf(roleStr);
        User newUser = null;

        try {
            switch (role) {

                case VOLUNTEER -> {
                    if (type == null) {
                        statusLabel.setText("Please select volunteer type!");
                        return;
                    }

                    newUser = switch (type) {
                        case "Person" -> new PersonVolunteer(
                                username, password, name, address, phone
                        );
                        case "Restaurant" -> new RestaurantVolunteer(
                                username, password, name, address, "My Restaurant", phone
                        );
                        case "Hotel" -> new HotelVolunteer(
                                username, password, name, address, "My Hotel", phone
                        );
                        default -> throw new IllegalArgumentException("Invalid volunteer type");
                    };
                }

                case DELIVERY -> newUser = new DeliveryPerson(
                        username, password, name, address, phone
                );

                case CHARITY -> newUser = new Charity(
                        username, password, name, address, phone
                );

                default -> throw new IllegalArgumentException("Unknown role");
            }

            userManager.addUser(newUser);
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
            StackPane stack = new StackPane();

            if (user instanceof Volunteer v) {
                loader = new FXMLLoader(getClass().getResource("/gui/views/volunteerDashboard.fxml"));
                stack.getChildren().add(loader.load());
                VolunteerDashboardController controller = loader.getController();
                controller.initData(v, FileManager.getNotificationManager());
            }
            else if (user instanceof Charity charity) {

                FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/gui/views/charityDashboard.fxml"));
                Parent root = loader1.load();

                CharityDashboardController controller = loader1.getController();
                NotificationManager nm = new NotificationManager();
                ComplaintManager cm = new ComplaintManager(nm);

                controller.initData(charity, cm);

                Stage stage = (Stage) registerButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
                return;
            }
            else if (user instanceof DeliveryPerson) {
                loader = new FXMLLoader(getClass().getResource("/gui/views/DeliveryDashboard.fxml"));
                stack.getChildren().add(loader.load());
            }
            else return;

            Stage dashboardStage = new Stage();
            Scene dashboardScene = new Scene(stack, 800, 600);
            dashboardStage.setScene(dashboardScene);
            dashboardStage.show();

            ((Stage) registerButton.getScene().getWindow()).close();

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), stack.getChildren().get(0));
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

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
        roleComboBox.setValue(null);
        volunteerTypeComboBox.setValue(null);
        volunteerTypeBox.setVisible(false);
    }
}
