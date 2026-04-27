package org.hotelNova.views.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.hotelNova.models.User;
import org.hotelNova.services.UserService;

public class RegisterController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    
    private final UserService userService = new UserService();
    
    @FXML
    public void initialize() {
        setupRoles();
    }
    
    private void setupRoles() {
        roleComboBox.getItems().addAll("ADMIN", "RECEPTIONIST", "MANAGER");
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
            
            userService.create(user);
            
            showSuccess("Usuario registrado exitosamente. Por favor inicie sesión.");
            
            // Redirigir al login después de 2 segundos
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
            stage.setTitle("Hotel Nova - Login");
            stage.setResizable(false);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading login view: " + e.getMessage());
            showError("Error al volver al login");
        }
    }
    
    private void validateForm() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String role = roleComboBox.getValue();
        
        if (username.isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es requerido");
        }
        
        if (username.length() < 3) {
            throw new IllegalArgumentException("El nombre de usuario debe tener al menos 3 caracteres");
        }
        
        if (password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña es requerida");
        }
        
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        
        if (role == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol");
        }
        
        // Verificar si el usuario ya existe
        try {
            userService.login(username, "dummy");
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        } catch (Exception e) {
            // Si falla el login, el usuario no existe, lo cual es bueno
            if (!e.getMessage().contains("Invalid login")) {
                throw e;
            }
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
