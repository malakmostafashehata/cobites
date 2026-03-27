package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;

// Main class to launch the JavaFX application
public class Main extends Application {

    private static final double WIDTH = 800;   // Width of the main window
    private static final double HEIGHT = 600;  // Height of the main window

    @Override
    public void start(Stage stage) throws Exception {
        // Load the welcome FXML as the root pane
        StackPane root = FXMLLoader.load(getClass().getResource("/gui/views/welcome.fxml"));
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Set the application icon
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/LOGO.jpg")));

        // Set the scene to the stage
        stage.setScene(scene);

        // Set window title
        stage.setTitle("COBITES");

        // Prevent resizing the window
        stage.setResizable(false);

        // Show the main stage
        stage.show();
    }

    // Launch the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}
