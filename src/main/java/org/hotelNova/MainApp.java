package org.hotelNova;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.hotelNova.config.DbInit;

public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            DbInit.init();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/login-view.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 500, 600);
            primaryStage.setTitle("Hotel Nova - Sign In");
            primaryStage.setResizable(false);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
