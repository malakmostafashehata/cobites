package gui.controllers;

import backend.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink;

    private final UserManager userManager = UserManager.getInstance();

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            loginButton.setOnAction(e -> handleLogin());
            registerLink.setOnAction(e -> openRegisterScreen());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();

            // Disable maximize for login window
            stage.setResizable(false);
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if(username.isEmpty() || password.isEmpty()){
            errorLabel.setText("Enter username and password!");
            return;
        }

        Optional<User> userOpt = userManager.checkLogin(username, password);

        if (userOpt.isEmpty()) {
            errorLabel.setText("Invalid username or password!");
            return;
        }

        openDashboardByRole(userOpt.get());
    }

    private void openDashboardByRole(User user) {
        try {
            FXMLLoader loader;
            StackPane stack = new StackPane();

            if(user instanceof Volunteer v){
                loader = new FXMLLoader(getClass().getResource("/gui/views/volunteerDashboard.fxml"));
                stack.getChildren().add(loader.load());
                VolunteerDashboardController controller = loader.getController();
                controller.initData(v, FileManager.getNotificationManager());

            } else if (user instanceof Charity loggedCharity) {

                // 1) Load FXML
                FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/gui/views/charityDashboard.fxml"));
                Parent root = loader1.load();

                // 2) Get controller
                CharityDashboardController controller = loader1.getController();

                // 3) Create managers
                NotificationManager nm = new NotificationManager();
                ComplaintManager complaintManager = new ComplaintManager(nm);

                // 4) Pass real charity + managers
                controller.initData(loggedCharity, complaintManager);

                // 5) Switch scene
                Stage stage1 = (Stage) loginButton.getScene().getWindow();
                stage1.setScene(new Scene(root));
                stage1.show();
            

            } else if(user instanceof Admin a){
                loader = new FXMLLoader(getClass().getResource("/gui/views/adminDashboard.fxml"));
                stack.getChildren().add(loader.load());

            } else if(user instanceof DeliveryPerson d){
                loader = new FXMLLoader(getClass().getResource("/gui/views/deliveryDashboard.fxml"));
                stack.getChildren().add(loader.load());

            } else return;

            // ===== Create new Stage for dashboard =====
            Stage dashboardStage = new Stage();
            Scene dashboardScene = new Scene(stack, 800, 600);
            dashboardStage.setScene(dashboardScene);
            dashboardStage.centerOnScreen();
            dashboardStage.setResizable(true); // dashboard can maximize
            dashboardStage.show();

            // Close login window
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();

            // ===== Optional: fade-in effect for dashboard =====
            stack.getChildren().get(0).setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), stack.getChildren().get(0));
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load dashboard!");
        }
    }

    private void openRegisterScreen() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/register.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);

            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();

            // Keep register window fixed size
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Failed to open Register screen!");
        }
    }
}
