package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

public class Main extends Application {

    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;

    @Override
    public void start(Stage stage) throws Exception {
        StackPane root = FXMLLoader.load(getClass().getResource("/gui/views/welcome.fxml"));
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setTitle("My App");
        stage.setResizable(false); // lock the size
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}