package org.hotelNova.views.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.hotelNova.DAO.BookingDAO;
import org.hotelNova.DAO.GuestDAO;
import org.hotelNova.DAO.RoomDAO;

import java.io.IOException;

public class MainController {
    
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private Label availableRoomsLabel;
    @FXML private Label activeBookingsLabel;
    @FXML private Label totalGuestsLabel;
    
    private final RoomDAO roomDAO = new RoomDAO();
    private final GuestDAO guestDAO = new GuestDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    
    @FXML
    public void initialize() {
        loadDashboardStats();
    }
    
    private void loadDashboardStats() {
        try {
            // Cargar estadísticas del dashboard
            int availableRooms = (int) roomDAO.findAll().stream()
                .filter(room -> room.isAvailable())
                .count();
            
            int totalGuests = guestDAO.findAll().size();
            
            int activeBookings = (int) bookingDAO.findAll().stream()
                .filter(booking -> "OCCUPIED".equals(booking.getStatus()))
                .count();
            
            availableRoomsLabel.setText(String.valueOf(availableRooms));
            totalGuestsLabel.setText(String.valueOf(totalGuests));
            activeBookingsLabel.setText(String.valueOf(activeBookings));
            
        } catch (Exception e) {
            System.err.println("Error loading dashboard stats: " + e.getMessage());
        }
    }
    
    @FXML
    private void showDashboard() {
        // Ocultar todas las vistas y mostrar dashboard
        contentArea.getChildren().forEach(node -> node.setVisible(false));
        dashboardView.setVisible(true);
        loadDashboardStats(); // Refrescar stats
    }
    
    @FXML
    private void showGuests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/guest-view.fxml"));
            Parent guestView = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(guestView);
            
        } catch (IOException e) {
            System.err.println("Error loading guest view: " + e.getMessage());
        }
    }
    
    @FXML
    private void showRooms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/room-view.fxml"));
            Parent roomView = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(roomView);
            
        } catch (IOException e) {
            System.err.println("Error loading room view: " + e.getMessage());
        }
    }
    
    @FXML
    private void showBookings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/booking-view.fxml"));
            Parent bookingView = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(bookingView);
            
        } catch (IOException e) {
            System.err.println("Error loading booking view: " + e.getMessage());
        }
    }
    
    @FXML
    private void showExport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/export-view.fxml"));
            Parent exportView = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(exportView);
            
        } catch (IOException e) {
            System.err.println("Error loading export view: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        // Cerrar la aplicación o volver al login
        System.exit(0);
    }
}
