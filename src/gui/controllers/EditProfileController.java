package gui.controllers;

import backend.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditProfileController {

    @FXML private TextField txtName;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtAddress;
    @FXML private Button btnSave;
    @FXML private Button btnLogout;
    @FXML private Button btnDelete;

    private Volunteer volunteer;

    public void initData(Volunteer v) {
        this.volunteer = v;

        txtName.setText(v.getName());
        txtPassword.setText(v.getPassword());
        txtAddress.setText(v.getAddress());
    }

    @FXML
    private void initialize() {

        btnSave.setOnAction(e -> {
            volunteer.setName(txtName.getText());
            volunteer.setPassword(txtPassword.getText());
            volunteer.setAddress(txtAddress.getText());

            FileManager.saveUsers(UserManager.getInstance().getAllUsers());

            closeWindow();
        });
        btnDelete.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete your account?",
                    ButtonType.YES, ButtonType.CANCEL);

            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    UserManager.getInstance().deleteUser(volunteer);
                    FileManager.saveUsers(UserManager.getInstance().getAllUsers());
                    closeWindow();
                }
            });
        });
    }
    
    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}
