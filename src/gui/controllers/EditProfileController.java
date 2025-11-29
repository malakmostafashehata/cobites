package gui.controllers;

import backend.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditProfileController {
	@FXML private TextField txtPhone;

    @FXML private TextField txtName;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtAddress;
    @FXML private Button btnSave;
    @FXML private TextField txtUsername;

    @FXML private Button btnDelete;

    @FXML private Label statusLabel;
    private Volunteer volunteer;

    public void initData(Volunteer v) {
        this.volunteer = v;

        txtName.setText(v.getName());
        txtPassword.setText(v.getPassword());
        txtAddress.setText(v.getAddress());
        txtPhone.setText(v.getPhone());
        txtUsername.setText(v.getUserName());

    }

    @FXML
    private void initialize() {

        btnSave.setOnAction(e -> {
            String newUsername = txtUsername.getText().trim();
            UserManager um = UserManager.getInstance();

            if(!newUsername.equals(volunteer.getUserName()) && um.usernameExists(newUsername)) {
                statusLabel.setText("Username already taken!");
                return;
            }

            um.updateUsername(volunteer, newUsername);

            volunteer.setPhone(txtPhone.getText().trim());
            volunteer.setName(txtName.getText().trim());
            volunteer.setPassword(txtPassword.getText());
            volunteer.setAddress(txtAddress.getText().trim());

            FileManager.saveUsers(UserManager.getInstance().getAllUsers()); // حفظ البيانات بعد التعديل
            statusLabel.setText("Profile updated successfully!");

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
