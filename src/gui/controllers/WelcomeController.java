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

public class WelcomeController {

    private static final double SCENE_WIDTH = 800;
    private static final double SCENE_HEIGHT = 600;
    private static final double LOGO_MAX_WIDTH = 300;
    private static final double LOGO_MAX_HEIGHT = 200;

    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImage;
    @FXML private ImageView logoImage;

    @FXML
    public void initialize() {
        // Load background
        Image bg = new Image(getClass().getResourceAsStream("/images/AAA.jpg"));
        backgroundImage.setImage(bg);

        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
        backgroundImage.setPreserveRatio(false);

        // Load logo
        Image logo = new Image(getClass().getResourceAsStream("/images/LOGO.jpg"));
        logoImage.setImage(logo);

        // Constrain logo size and center
        logoImage.setFitWidth(LOGO_MAX_WIDTH);
        logoImage.setFitHeight(LOGO_MAX_HEIGHT);
        logoImage.setPreserveRatio(true);
        StackPane.setAlignment(logoImage, Pos.CENTER);

        // Play animation after scene is ready
        Platform.runLater(this::playWelcomeAnimation);
    }

    private void playWelcomeAnimation() {
        // Fade + scale animation for logo
        FadeTransition fade = new FadeTransition(Duration.seconds(2), logoImage);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(2), logoImage);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition anim = new ParallelTransition(fade, scale);
        anim.play();

        anim.setOnFinished(event -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(ev -> fadeOutToLogin());
            pause.play();
        });
    }

    private void fadeOutToLogin() {
        Stage stage = (Stage) rootPane.getScene().getWindow();

        try {
            // Load login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/views/login.fxml"));
            StackPane loginRoot = loader.load();
            loginRoot.setOpacity(0);

            // Overlay login on top of welcome
            StackPane stack = new StackPane();
            stack.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT);
            stack.getChildren().addAll(loginRoot, rootPane);

            Scene scene = new Scene(stack, SCENE_WIDTH, SCENE_HEIGHT);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setResizable(false);

            // Fade out entire welcome page (background + logo)
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), rootPane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> stack.getChildren().remove(rootPane));
            fadeOut.play();

            // Fade in login page
            FadeTransition fadeInLogin = new FadeTransition(Duration.seconds(0.5), loginRoot);
            fadeInLogin.setFromValue(0);
            fadeInLogin.setToValue(1);
            fadeInLogin.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}