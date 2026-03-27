package gui.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

// Controller for the welcome/splash screen
public class WelcomeController {

    private static final double SCENE_WIDTH = 800;       // Width of the scene
    private static final double SCENE_HEIGHT = 600;      // Height of the scene
    private static final double LOGO_MAX_WIDTH = 300;    // Max width of the logo
    private static final double LOGO_MAX_HEIGHT = 200;   // Max height of the logo

    @FXML private StackPane rootPane;       // Root container of the welcome screen
    @FXML private ImageView backgroundImage; // Background image view
    @FXML private ImageView logoImage;       // Logo image view

    @FXML
    public void initialize() {
        // Load background image
        Image bg = new Image(getClass().getResourceAsStream("/images/login.jpeg"));
        backgroundImage.setImage(bg);

        // Make background fill the root pane
        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
        backgroundImage.setPreserveRatio(false);

        // Load logo image
        Image logo = new Image(getClass().getResourceAsStream("/images/LOGO.jpg"));
        logoImage.setImage(logo);

        // Constrain logo size and center it
        logoImage.setFitWidth(LOGO_MAX_WIDTH);
        logoImage.setFitHeight(LOGO_MAX_HEIGHT);
        logoImage.setPreserveRatio(true);
        StackPane.setAlignment(logoImage, Pos.CENTER);

        // Play welcome animation after the scene is ready
        Platform.runLater(this::playWelcomeAnimation);
    }

    // Plays the fade-in + scale animation for the logo
    private void playWelcomeAnimation() {
        // Fade-in transition for the logo
        FadeTransition fade = new FadeTransition(Duration.seconds(2), logoImage);
        fade.setFromValue(0);
        fade.setToValue(1);

        // Scale-up transition for the logo
        ScaleTransition scale = new ScaleTransition(Duration.seconds(2), logoImage);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1);
        scale.setToY(1);

        // Play fade and scale transitions together
        ParallelTransition anim = new ParallelTransition(fade, scale);
        anim.play();

        // After animation finishes, pause for 1 second before fading out to login
        anim.setOnFinished(event -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(ev -> fadeOutToLogin());
            pause.play();
        });
    }

    // Handles transition from welcome screen to login screen
    private void fadeOutToLogin() {
        Stage stage = (Stage) rootPane.getScene().getWindow();

        try {
            // Load login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/login.fxml"));
            StackPane loginRoot = loader.load();
            loginRoot.setOpacity(0); // Start login page invisible

            // Overlay login page on top of welcome screen
            StackPane stack = new StackPane();
            stack.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
            stack.getChildren().addAll(loginRoot, rootPane);

            // Set new scene with stacked panes
            Scene scene = new Scene(stack, SCENE_WIDTH, SCENE_HEIGHT);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setResizable(false);

            // Fade out the welcome page (background + logo)
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), rootPane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> stack.getChildren().remove(rootPane));
            fadeOut.play();

            // Fade in the login page
            FadeTransition fadeInLogin = new FadeTransition(Duration.seconds(0.5), loginRoot);
            fadeInLogin.setFromValue(0);
            fadeInLogin.setToValue(1);
            fadeInLogin.play();

        } catch (Exception e) {
            // Print any exceptions during loading or animation
            e.printStackTrace();
        }
    }
}
