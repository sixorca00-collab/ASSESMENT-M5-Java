package org.hotelNova.views.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.hotelNova.models.User;
import org.hotelNova.services.UserService;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    private final UserService userService = new UserService();
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter your username and password.");
            return;
        }
        
        try {
            User user = userService.login(username, password);
            
            if (user != null) {
                openMainView(user);
            } else {
                showError("Invalid username or password.");
            }
            
        } catch (Exception e) {
            showError("Sign-in failed: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExit() {
        System.exit(0);
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    private void openMainView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/main-view.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setCurrentUser(user);
            
            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            stage.setScene(scene);
            stage.setTitle("Hotel Nova - Operations");
            stage.setResizable(true);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error opening main view: " + e.getMessage());
            showError("Unable to open the main application.");
        }
    }
    
    @FXML
    private void handleGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/register-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 620, 780);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            stage.setScene(scene);
            stage.setTitle("Hotel Nova - Register");
            stage.setResizable(true);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error opening register view: " + e.getMessage());
            showError("Unable to open the registration screen.");
        }
    }
}
