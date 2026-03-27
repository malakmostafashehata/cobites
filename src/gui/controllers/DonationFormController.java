package gui.controllers;

import backend.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class DonationFormController {

    @FXML private ComboBox<FoodType> cmbType;
    @FXML private TextField txtName;
    @FXML private TextField txtQty;
    @FXML private Button btnBrowse;
    @FXML private Label lblImage;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;
    @FXML private Label errName;
    @FXML private Label errQty;
    @FXML private Label lblStatus;
    @FXML private Label lblTitle;

    private Volunteer volunteer;

    private FoodItem existingItem;
    private String imagePath = "";

    public void initData(Volunteer volunteer, FoodItem item){
        this.volunteer = volunteer;
        this.existingItem = item;

        cmbType.getItems().addAll(FoodType.values());
        cmbType.getSelectionModel().selectFirst();

        if(existingItem != null){
            txtName.setText(existingItem.getName());
            txtQty.setText(String.valueOf(existingItem.getQty()));
            cmbType.getSelectionModel().select(existingItem.getType());
            lblImage.setText(existingItem.getImagePath() != null ? new File(existingItem.getImagePath()).getName() : "");
            imagePath = existingItem.getImagePath();
        }

        lblStatus.setText("");
        setEditMode(existingItem != null);

        btnBrowse.setOnAction(e -> chooseImage());
        btnCancel.setOnAction(e -> closeForm());
        btnSubmit.setOnAction(e -> submitDonation());
    }

    private void chooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Donation Image");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", ".png", ".jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(null);
        if (file != null) {
            imagePath = file.getAbsolutePath();  
            lblImage.setText(file.getName());
        }
    }

    private void submitDonation() {
        clearErrors();
        lblStatus.setText(""); // Clear previous messages

        String name = txtName.getText().trim();
        String qtyStr = txtQty.getText().trim();
        boolean valid = true;
        int qty = 0;

        // 1️⃣ Validate name first
        if (name.isEmpty()) {
            lblStatus.setText("Name is required.");
            valid = false;
        } else if (!name.matches("[a-zA-Z]+")) {
            lblStatus.setText("Name must contain only letters.");
            valid = false;
        }

        // 2️⃣ Validate quantity next
        if (valid) { // Only check quantity if name is valid
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    lblStatus.setText("Quantity must be greater than 0.");
                    valid = false;
                }
            } catch (Exception e) {
                lblStatus.setText("Enter a valid number.");
                valid = false;
            }
        }

        // Validate food type
        if (valid && cmbType.getValue() == null) {
            lblStatus.setText("Please select a type.");
            valid = false;
        }

        if (!valid) return;

        final String finalName = name;
        final int finalQty = qty;
        final FoodType finalType = cmbType.getValue();
        final String finalImagePath = imagePath;

        showConfirmationDialog(finalName, finalQty, finalType, finalImagePath);
    }




    private void showConfirmationDialog(String name, int qty, FoodType type, String imagePath) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #162447;"); // dark background

        // Title
        Label lblHeader = new Label("Confirm your donation");
        lblHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");

        // Donation details
        Label lblName = new Label("Name: " + name);
        Label lblQty = new Label("Quantity: " + qty);
        Label lblType = new Label("Type: " + type.name());
        lblName.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        lblQty.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        lblType.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Warning
        Label lblWarning = new Label("Once added, it cannot be changed or deleted.");
        lblWarning.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        content.getChildren().addAll(lblHeader, lblName, lblQty, lblType, lblWarning);

        // Create Alert
        Alert alert = new Alert(Alert.AlertType.NONE); // use NONE to fully customize buttons
        alert.setTitle("Confirm Donation");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.getDialogPane().setContent(content);

        // Set dark background for entire dialog (including button area)
        alert.getDialogPane().setStyle("-fx-background-color: #162447;");

        // Create Yes / No buttons
        ButtonType yesButtonType = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButtonType = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButtonType, noButtonType);

        // Style buttons
        Button btnYes = (Button) alert.getDialogPane().lookupButton(yesButtonType);
        Button btnNo = (Button) alert.getDialogPane().lookupButton(noButtonType);

        String btnStyle = "-fx-background-color: #162447; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-border-width: 1.5;" +
                "-fx-font-weight: bold";
        String btnHover = "-fx-background-color: #1F3B73; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: white; -fx-border-width: 1.5;" +
                "-f-font-weight: bold";

        btnYes.setStyle(btnStyle);
        btnNo.setStyle(btnStyle);

        btnYes.setOnMouseEntered(e -> btnYes.setStyle(btnHover));
        btnYes.setOnMouseExited(e -> btnYes.setStyle(btnStyle));

        btnNo.setOnMouseEntered(e -> btnNo.setStyle(btnHover));
        btnNo.setOnMouseExited(e -> btnNo.setStyle(btnStyle));

        // Show alert and handle response
        alert.showAndWait().ifPresent(response -> {
        	if (response == yesButtonType) {

        	    String id = java.util.UUID.randomUUID().toString().substring(0, 8);

        	    FoodItem item = new FoodItem(
        	        id,
        	        name,
        	        qty,
        	        volunteer.getName(),
        	        type,
        	        imagePath,
        	        java.time.LocalDate.now()
        	    );

        	    Donation donation = new Donation(
        	        java.util.UUID.randomUUID().toString().substring(0, 8),
        	        item,
        	        volunteer,
        	        null,
        	        java.time.LocalDate.now()
        	    );
        	    item.setDonorAddress(volunteer.getAddress());
        	    item.setDonorPhone(volunteer.getPhone());

        	    donation.setDonorPhone(volunteer.getPhone());

        	    FileManager.saveDonation(donation);


        	    volunteer.addDonation(donation);

        	    Stage stage = (Stage) btnSubmit.getScene().getWindow();
        	    stage.close();
        	}

        });

    }


    private void clearErrors(){
        errName.setText("");
        errQty.setText("");
    }

    private void closeForm(){
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public void setEditMode(boolean isEdit){
        lblTitle.setText(isEdit ? "Edit Donation" : "Add New Donation");
    }
}