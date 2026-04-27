package org.hotelNova.views.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.hotelNova.models.Guest;
import org.hotelNova.models.User;
import org.hotelNova.services.GuestService;
import org.hotelNova.services.UserService;

public class RegisterController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private VBox guestFieldsContainer;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label accountSectionLabel;
    @FXML private TextField guestNameField;
    @FXML private TextField guestDocumentField;
    @FXML private TextField guestPhoneField;
    @FXML private TextField guestEmailField;
    @FXML private Label roleHelpLabel;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    
    private final UserService userService = new UserService();
    private final GuestService guestService = new GuestService();
    
    @FXML
    public void initialize() {
        setupRoles();
    }
    
    private void setupRoles() {
        roleComboBox.getItems().setAll("RECEPTIONIST", "GUEST");
        handleRoleChange();
    }

    @FXML
    private void handleRoleChange() {
        String selectedRole = roleComboBox.getValue();
        boolean guestRole = "GUEST".equals(selectedRole);
        guestFieldsContainer.setVisible(guestRole);
        guestFieldsContainer.setManaged(guestRole);
        if (guestRole) {
            titleLabel.setText("Create Guest Account");
            subtitleLabel.setText("Create guest access and link it to the guest profile below.");
            accountSectionLabel.setText("Guest Access");
            roleHelpLabel.setText("Guest accounts can only review their own bookings.");
            registerButton.setText("Create Guest Account");
        } else if ("RECEPTIONIST".equals(selectedRole)) {
            titleLabel.setText("Create Receptionist Account");
            subtitleLabel.setText("Create a front-desk account for guest and booking operations.");
            accountSectionLabel.setText("Receptionist Access");
            roleHelpLabel.setText("Receptionist accounts can manage guests and bookings from the front desk.");
            registerButton.setText("Create Receptionist Account");
        } else {
            titleLabel.setText("Create Account");
            subtitleLabel.setText("Choose a role to continue the registration flow.");
            accountSectionLabel.setText("Account Access");
            roleHelpLabel.setText("Select whether this account is for the reception desk or a guest.");
            registerButton.setText("Register");
        }
    }
    
    @FXML
    private void handleRegister() {
        try {
            validateForm();
            
            User user = new User();
            user.setUsername(usernameField.getText().trim());
            user.setPassword(passwordField.getText().trim());
            user.setRol(roleComboBox.getValue());
            user.setActive(true);

            if ("GUEST".equals(roleComboBox.getValue())) {
                Guest guest = new Guest();
                guest.setName(guestNameField.getText().trim());
                guest.setDocument(guestDocumentField.getText().trim());
                guest.setPhone(guestPhoneField.getText().trim());
                guest.setEmail(guestEmailField.getText().trim());
                guest.setActive(true);
                userService.createGuestAccount(user, guest);
            } else {
                userService.create(user);
            }
            
            showSuccess("User registered successfully. Please sign in.");
            
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(2000);
                    handleBackToLogin();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/login-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 500, 600);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            stage.setScene(scene);
            stage.setTitle("Hotel Nova - Sign In");
            stage.setResizable(false);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading login view: " + e.getMessage());
            showError("Unable to return to the sign-in screen.");
        }
    }
    
    private void validateForm() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String role = roleComboBox.getValue();
        
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long.");
        }
        
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        
        if (role == null) {
            throw new IllegalArgumentException("Please select a role.");
        }

        if (userService.usernameExists(username)) {
            throw new IllegalArgumentException("That username already exists.");
        }

        if ("GUEST".equals(role)) {
            validateGuestFields();
        }
    }

    private void validateGuestFields() {
        if (guestNameField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Guest full name is required.");
        }
        if (guestDocumentField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Guest document ID is required.");
        }
        if (guestPhoneField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Guest phone is required.");
        }
        if (guestEmailField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Guest email is required.");
        }
        if (guestService.findByDocument(guestDocumentField.getText().trim()) != null) {
            throw new IllegalArgumentException("A guest with that document ID already exists.");
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }
    
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
