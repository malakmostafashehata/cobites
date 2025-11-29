
package gui.controllers;

import backend.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML private Label lblTitle; // dynamic title

    private Volunteer volunteer;
    private NotificationManager notificationManager;
    private FoodItem existingItem; // for edit mode
    private String imagePath = "";

    // Initialize the form with data
    public void initData(Volunteer v, NotificationManager nm, FoodItem item){
        this.volunteer = v;
        this.notificationManager = nm;
        this.existingItem = item;

        cmbType.getItems().addAll(FoodType.values());
        cmbType.getSelectionModel().selectFirst();

        // If editing, populate existing data
        if(existingItem != null){
            txtName.setText(existingItem.getName());
            txtQty.setText(String.valueOf(existingItem.getQty()));
            cmbType.getSelectionModel().select(existingItem.getType());
            lblImage.setText(existingItem.getImagePath() != null ? new File(existingItem.getImagePath()).getName() : "");
            imagePath = existingItem.getImagePath();
        }

        // Clear status label when opening
        lblStatus.setText("");

        // Set dynamic title
        setEditMode(existingItem != null);

        // Button actions
        btnBrowse.setOnAction(e -> chooseImage());
        btnCancel.setOnAction(e -> closeForm());
        btnSubmit.setOnAction(e -> submitDonation());
    }

    // Choose image for donation
    private void chooseImage(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Donation Image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(null);
        if(file != null){
            imagePath = file.getAbsolutePath();
            lblImage.setText(file.getName());
        }
    }

    // Submit new or edited donation
    private void submitDonation(){
        clearErrors();
        lblStatus.setText("");

        String name = txtName.getText().trim();
        String qtyStr = txtQty.getText().trim();

        boolean valid = true;
        int qty = 0;

        if(name.isEmpty()){
            errName.setText("Name is required.");
            valid = false;
        }

        try {
            qty = Integer.parseInt(qtyStr);
            if(qty <= 0){
                errQty.setText("Quantity must be > 0.");
                valid = false;
            }
        } catch(Exception e){
            errQty.setText("Enter a valid number.");
            valid = false;
        }

        if(cmbType.getValue() == null){
            lblStatus.setStyle("-fx-text-fill: red;");
            lblStatus.setText("Please select a type.");
            valid = false;
        }

        if(!valid) return;

        if(existingItem == null){
            // Add new donation
            FoodItem item = new FoodItem(
                    java.util.UUID.randomUUID().toString(),
                    name,
                    qty,
                    volunteer.getName(),
                    cmbType.getValue(),
                    imagePath,
                    java.time.LocalDate.now()
            );

            Donation donation = new Donation(
                    java.util.UUID.randomUUID().toString(),
                    item,
                    volunteer,
                    java.time.LocalDate.now()
            );

            volunteer.addDonation(donation);
            FileManager.saveDonation(donation);
            notificationManager.add("New donation from " + volunteer.getName() + " Type: " + cmbType.getValue());
        } else {
            // Edit existing donation in memory
            existingItem.setName(name);
            existingItem.setQty(qty);
            existingItem.setType(cmbType.getValue());
            existingItem.setImagePath(imagePath);

            // Save the updated donation
            Donation donationToSave = new Donation(existingItem.getId(), existingItem, volunteer, existingItem.getDonationDate());
            FileManager.updateDonation(donationToSave);

            notificationManager.add("Donation updated for " + volunteer.getName());
        }

        lblStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        lblStatus.setText(existingItem == null ? "Donation submitted successfully!" : "Donation updated successfully!");

        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }

    // Clear validation errors
    private void clearErrors(){
        errName.setText("");
        errQty.setText("");
    }

    // Close form
    private void closeForm(){
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    // Set dynamic title
    public void setEditMode(boolean isEdit) {
        lblTitle.setText(isEdit ? "Edit Donation" : "Add New Donation");
    }
}
