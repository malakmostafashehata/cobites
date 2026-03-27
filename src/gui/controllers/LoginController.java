package gui.controllers;

import backend.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink registerLink;
    @FXML private Hyperlink btnForgotPassword;

    private final UserManager userManager = UserManager.getInstance();

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            loginButton.setOnAction(e -> handleLogin());
            registerLink.setOnAction(e -> openRegisterScreen());
            btnForgotPassword.setOnAction(e -> openForgotPasswordDialog());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setResizable(false);
            stage.centerOnScreen();
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
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
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader;
            Parent root;

            // ===== Charity =====
            if (user instanceof Charity loggedCharity) {

                if (!"accepted".equalsIgnoreCase(loggedCharity.getReviewStatus())) {
                    errorLabel.setText("Your registration is under review. You cannot login yet.");
                    return;
                }

                loader = new FXMLLoader(getClass().getResource("/gui/views/charityDashboard.fxml"));
                root = loader.load();
                ((CharityDashboardController) loader.getController()).initData(loggedCharity);
            }

            // ===== Volunteer =====
            else if (user instanceof Volunteer v) {
                loader = new FXMLLoader(getClass().getResource("/gui/views/volunteerDashboard.fxml"));
                root = loader.load();
                ((VolunteerDashboardController) loader.getController()).initData(v);
            }

            // ===== Admin =====
            else if (user instanceof Admin a) {
                loader = new FXMLLoader(getClass().getResource("/gui/views/AdminDashboard.fxml"));
                root = loader.load();
                ((AdminDashboardController) loader.getController()).initData(a);
            }
            // ===== Delivery =====
            else if (user instanceof DeliveryPerson d) {
                loader = new FXMLLoader(getClass().getResource("/gui/views/deliveryDashboard.fxml"));
                root = loader.load();
                ((DeliveryDashboardController) loader.getController()).initData(d);
            }

            else {
                errorLabel.setText("Unknown user role");
                return;
            }

            // ===== Fade Fix =====
            root.setOpacity(0);                 
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();

            // ===== Fade Animation =====
            FadeTransition fade = new FadeTransition(Duration.millis(300), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load dashboard!");
        }
    }



    private void openFullScreenDashboard(
            String fxmlPath,
            ControllerInitializer initializer,
            Stage stage
    ) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        initializer.init(loader.getController());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.centerOnScreen();
    }


    private void openRegisterScreen() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/register.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Failed to open Register screen!");
        }
    }

    private void openForgotPasswordDialog() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #162447; -fx-padding: 20;");

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 0 0 5 0;");
        errorLbl.setVisible(false);
        errorLbl.setWrapText(true);
        errorLbl.setMaxWidth(300);
        errorLbl.setAlignment(Pos.CENTER_LEFT);

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");
        usernameInput.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #CCCCCC;" +
                "-fx-border-color: white;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 5 10;"
        );
        usernameInput.setPrefWidth(200);

        TextField phoneInput = new TextField();
        phoneInput.setPromptText("Phone");
        phoneInput.setStyle(usernameInput.getStyle());
        phoneInput.setPrefWidth(200);

        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        usernameLabel.setMinWidth(70);

        Label phoneLabel = new Label("Phone:");
        phoneLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        phoneLabel.setMinWidth(70);

        HBox usernameBox = new HBox(10, usernameLabel, usernameInput);
        usernameBox.setAlignment(Pos.CENTER_LEFT);

        HBox phoneBox = new HBox(10, phoneLabel, phoneInput);
        phoneBox.setAlignment(Pos.CENTER_LEFT);

        VBox vbox = new VBox(10, usernameBox, phoneBox, errorLbl);
        vbox.setPadding(new Insets(5, 10, 0, 10));
        dialogPane.setContent(vbox);
        dialogPane.setPrefWidth(400);

        Button ok = (Button) dialogPane.lookupButton(ButtonType.OK);
        ok.setText("Resend Password");
        ok.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: white;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 8;" +
                "-fx-padding: 5 12;" +
                "-fx-font-weight: bold;"
        );

        Button cancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancel.setStyle(ok.getStyle());

        ok.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            String username = usernameInput.getText().trim();
            String phone = phoneInput.getText().trim();

            if (username.isEmpty()) {
                errorLbl.setText("Username is required.");
                errorLbl.setVisible(true);
                event.consume();
                return;
            }

            if (phone.isEmpty()) {
                errorLbl.setText("Phone number is required.");
                errorLbl.setVisible(true);
                event.consume();
                return;
            }

            if (!phone.matches("\\d{11}")) {
                errorLbl.setText("Phone number must be exactly 11 digits.");
                errorLbl.setVisible(true);
                event.consume();
                return;
            }

            User found = UserManager.getInstance().getAllUsers().stream()
                    .filter(u -> u.getUserName().equals(username) && u.getPhone().equals(phone))
                    .findFirst()
                    .orElse(null);

            if (found == null) {
                errorLbl.setText("Account not found.");
                errorLbl.setVisible(true);
                event.consume();
                return;
            }

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Password Reset");
                alert.setHeaderText(null);
                alert.setContentText("Your password will be sent to your email.");
                alert.setGraphic(null);

                DialogPane alertPane = alert.getDialogPane();
                alertPane.setStyle(
                        "-fx-background-color: #162447;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 20;"
                );

                Label contentLabel = (Label) alertPane.lookup(".content.label");
                if (contentLabel != null) {
                    contentLabel.setStyle(
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;"
                    );
                }

                Button okButton = (Button) alertPane.lookupButton(ButtonType.OK);
                if (okButton != null) {
                    okButton.setStyle(
                            "-fx-background-color: transparent;" +
                            "-fx-text-fill: white;" +
                            "-fx-border-color: white;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 8;" +
                            "-fx-padding: 5 12;" +
                            "-fx-font-weight: bold;"
                    );
                }

                alert.showAndWait();
            });
        });

        dialog.showAndWait();
    }

    @FunctionalInterface interface ControllerInitializer {
        void init(Object controller);
    }
}
