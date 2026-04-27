package org.hotelNova.views.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.hotelNova.DAO.BookingDAO;
import org.hotelNova.DAO.GuestDAO;
import org.hotelNova.DAO.RoomDAO;
import org.hotelNova.models.User;

import java.io.IOException;

public class MainController {
    
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private Label sessionLabel;
    @FXML private Label dashboardTitleLabel;
    @FXML private Label dashboardSubtitleLabel;
    @FXML private Label welcomeTitleLabel;
    @FXML private Label welcomeSubtitleLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label activeBookingsLabel;
    @FXML private Label totalGuestsLabel;
    @FXML private javafx.scene.control.Button dashboardNavButton;
    @FXML private javafx.scene.control.Button guestNavButton;
    @FXML private javafx.scene.control.Button roomsNavButton;
    @FXML private javafx.scene.control.Button bookingsNavButton;
    @FXML private javafx.scene.control.Button exportNavButton;
    @FXML private javafx.scene.control.Button primaryQuickActionButton;
    @FXML private javafx.scene.control.Button secondaryQuickActionButton;
    @FXML private javafx.scene.control.Button tertiaryQuickActionButton;
    
    private final RoomDAO roomDAO = new RoomDAO();
    private final GuestDAO guestDAO = new GuestDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private User currentUser;
    private String currentRole = "ADMIN";
    
    @FXML
    public void initialize() {
        loadDashboardStats();
        configureForRole();
    }
    
    private void loadDashboardStats() {
        try {
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

    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.currentRole = normalizeRole(user.getRol());
        configureForRole();
        loadDashboardStats();
    }

    private void configureForRole() {
        String username = currentUser != null ? currentUser.getUsername() : "Operator";
        sessionLabel.setText(username + " • " + currentRole);

        switch (currentRole) {
            case "MANAGER" -> configureManagerView();
            case "RECEPTIONIST" -> configureReceptionistView();
            case "GUEST" -> configureGuestView();
            case "ADMIN" -> configureAdminView();
            default -> configureAdminView();
        }

        showDashboardView();
    }

    private void configureAdminView() {
        dashboardTitleLabel.setText("Admin Dashboard");
        dashboardSubtitleLabel.setText("Full operational control across guests, rooms, bookings, and exports.");
        welcomeTitleLabel.setText("Run the hotel from one place");
        welcomeSubtitleLabel.setText("Review occupancy, update room inventory, manage guest records, and export data when needed.");
        setNavVisibility(guestNavButton, true);
        setNavVisibility(roomsNavButton, true);
        setNavVisibility(bookingsNavButton, true);
        setNavVisibility(exportNavButton, true);
        configureQuickAction(primaryQuickActionButton, "Create Booking", this::showBookings, true, true);
        configureQuickAction(secondaryQuickActionButton, "Register Guest", this::showGuests, true, true);
        configureQuickAction(tertiaryQuickActionButton, "Export Data", this::showExport, true, true);
    }

    private void configureManagerView() {
        dashboardTitleLabel.setText("Manager Overview");
        dashboardSubtitleLabel.setText("Track occupancy, oversee room inventory, and review booking flow.");
        welcomeTitleLabel.setText("Focus on operations and planning");
        welcomeSubtitleLabel.setText("This workspace prioritizes oversight: room performance, occupancy status, and export-ready reporting.");
        setNavVisibility(guestNavButton, false);
        setNavVisibility(roomsNavButton, true);
        setNavVisibility(bookingsNavButton, true);
        setNavVisibility(exportNavButton, true);
        configureQuickAction(primaryQuickActionButton, "Review Rooms", this::showRooms, true, true);
        configureQuickAction(secondaryQuickActionButton, "View Bookings", this::showBookings, true, true);
        configureQuickAction(tertiaryQuickActionButton, "Open Exports", this::showExport, true, true);
    }

    private void configureReceptionistView() {
        dashboardTitleLabel.setText("Reception Desk");
        dashboardSubtitleLabel.setText("Handle arrivals, guest registration, and live front-desk booking activity.");
        welcomeTitleLabel.setText("Front-desk tools only");
        welcomeSubtitleLabel.setText("Receptionists can work with guests and bookings while room inventory management stays read-only.");
        setNavVisibility(guestNavButton, true);
        setNavVisibility(roomsNavButton, true);
        setNavVisibility(bookingsNavButton, true);
        setNavVisibility(exportNavButton, false);
        configureQuickAction(primaryQuickActionButton, "New Check-In", this::showBookings, true, true);
        configureQuickAction(secondaryQuickActionButton, "Add Guest", this::showGuests, true, true);
        configureQuickAction(tertiaryQuickActionButton, "View Rooms", this::showRooms, true, true);
    }

    private void configureGuestView() {
        dashboardTitleLabel.setText("Guest Portal");
        dashboardSubtitleLabel.setText("Review your booking history and current reservation status.");
        welcomeTitleLabel.setText("Your reservations in one place");
        welcomeSubtitleLabel.setText("Guest accounts can only access their own bookings from this portal.");
        setNavVisibility(guestNavButton, false);
        setNavVisibility(roomsNavButton, false);
        setNavVisibility(bookingsNavButton, true);
        setNavVisibility(exportNavButton, false);
        configureQuickAction(primaryQuickActionButton, "View My Bookings", this::showBookings, true, true);
        configureQuickAction(secondaryQuickActionButton, "Refresh Dashboard", this::showDashboard, true, true);
        configureQuickAction(tertiaryQuickActionButton, "", this::showDashboard, false, false);
    }

    private void setNavVisibility(javafx.scene.control.Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private void configureQuickAction(javafx.scene.control.Button button, String text, Runnable action, boolean visible, boolean primary) {
        button.setText(text);
        button.setVisible(visible);
        button.setManaged(visible);
        button.setOnAction(event -> action.run());
        if (primary) {
            button.getStyleClass().setAll("button-primary");
        } else {
            button.getStyleClass().setAll("button-secondary");
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "ADMIN";
        }

        return switch (role.trim().toUpperCase()) {
            case "ADMIN" -> "ADMIN";
            case "MANAGER" -> "MANAGER";
            case "RECEPTIONIST", "RECEPCIONISTA" -> "RECEPTIONIST";
            case "GUEST", "HUESPED", "HUÉSPED" -> "GUEST";
            default -> role.trim().toUpperCase();
        };
    }

    private void showDashboardView() {
        contentArea.getChildren().setAll(dashboardView);
    }
    
    @FXML
    private void showDashboard() {
        showDashboardView();
        loadDashboardStats();
    }
    
    @FXML
    private void showGuests() {
        if ("GUEST".equals(currentRole) || "MANAGER".equals(currentRole)) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/guest-view.fxml"));
            Parent guestView = loader.load();
            org.hotelNova.views.controllers.GuestController controller = loader.getController();
            controller.configureForRole(currentRole);
            
            contentArea.getChildren().setAll(guestView);
            
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
            org.hotelNova.views.controllers.RoomController controller = loader.getController();
            controller.configureForRole(currentRole);
            
            contentArea.getChildren().setAll(roomView);
            
        } catch (IOException e) {
            System.err.println("Error loading room view: " + e.getMessage());
        }
    }
    
    @FXML
    private void showBookings() {
        if ("GUEST".equals(currentRole)) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/booking-view.fxml"));
            Parent bookingView = loader.load();
            org.hotelNova.views.controllers.BookingController controller = loader.getController();
            controller.configureForUser(currentUser);
            
            contentArea.getChildren().setAll(bookingView);
            
        } catch (IOException e) {
            System.err.println("Error loading booking view: " + e.getMessage());
        }
    }
    
    @FXML
    private void showExport() {
        if ("RECEPTIONIST".equals(currentRole) || "GUEST".equals(currentRole)) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/export-view.fxml"));
            Parent exportView = loader.load();
            org.hotelNova.views.controllers.ExportController controller = loader.getController();
            controller.configureForRole(currentRole);
            
            contentArea.getChildren().setAll(exportView);
            
        } catch (IOException e) {
            System.err.println("Error loading export view: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/org/hotelNova/views/fxml/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("Hotel Nova - Sign In");
            stage.setResizable(false);
        } catch (IOException e) {
            System.err.println("Error loading login view: " + e.getMessage());
        }
    }
}
