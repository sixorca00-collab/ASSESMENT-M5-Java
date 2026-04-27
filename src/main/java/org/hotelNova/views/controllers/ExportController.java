package org.hotelNova.views.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import org.hotelNova.DAO.BookingDAO;
import org.hotelNova.DAO.GuestDAO;
import org.hotelNova.DAO.RoomDAO;
import org.hotelNova.models.Booking;
import org.hotelNova.models.Guest;
import org.hotelNova.models.Room;
import org.hotelNova.utils.CsvExporter;

import java.io.File;
import java.util.List;

public class ExportController {
    
    @FXML private CheckBox exportGuestsCheckBox;
    @FXML private CheckBox exportRoomsCheckBox;
    @FXML private CheckBox exportBookingsCheckBox;
    @FXML private TextField pathField;
    @FXML private Label exportErrorLabel;
    @FXML private Label exportSuccessLabel;
    @FXML private VBox resultsContainer;
    @FXML private TableView<ExportResult> resultsTable;
    @FXML private TableColumn<ExportResult, String> entityColumn;
    @FXML private TableColumn<ExportResult, Integer> recordsColumn;
    @FXML private TableColumn<ExportResult, String> fileColumn;
    @FXML private TableColumn<ExportResult, String> statusColumn;
    
    private final GuestDAO guestDAO = new GuestDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ObservableList<ExportResult> exportResults = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupResultsTable();
    }
    
    private void setupResultsTable() {
        entityColumn.setCellValueFactory(new PropertyValueFactory<>("entity"));
        recordsColumn.setCellValueFactory(new PropertyValueFactory<>("recordCount"));
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        resultsTable.setItems(exportResults);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    @FXML
    private void handleBrowsePath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Directorio de Exportación");
        
        File initialDir = new File(pathField.getText());
        if (initialDir.exists()) {
            directoryChooser.setInitialDirectory(initialDir);
        }
        
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            pathField.setText(selectedDirectory.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleExport() {
        try {
            exportResults.clear();
            hideMessages();
            
            String basePath = pathField.getText().trim();
            if (basePath.isEmpty()) {
                showError("Por favor seleccione un directorio de exportación");
                return;
            }
            
            boolean hasSelection = exportGuestsCheckBox.isSelected() || 
                                exportRoomsCheckBox.isSelected() || 
                                exportBookingsCheckBox.isSelected();
            
            if (!hasSelection) {
                showError("Por favor seleccione al menos una entidad para exportar");
                return;
            }
            
            // Export Guests
            if (exportGuestsCheckBox.isSelected()) {
                exportGuests(basePath);
            }
            
            // Export Rooms
            if (exportRoomsCheckBox.isSelected()) {
                exportRooms(basePath);
            }
            
            // Export Bookings
            if (exportBookingsCheckBox.isSelected()) {
                exportBookings(basePath);
            }
            
            // Show results
            resultsContainer.setVisible(true);
            resultsContainer.setManaged(true);
            
            showSuccess("Exportación completada. Revise los resultados detallados.");
            
        } catch (Exception e) {
            showError("Error durante la exportación: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClear() {
        exportGuestsCheckBox.setSelected(true);
        exportRoomsCheckBox.setSelected(true);
        exportBookingsCheckBox.setSelected(true);
        pathField.setText("exports/");
        hideMessages();
        resultsContainer.setVisible(false);
        resultsContainer.setManaged(false);
        exportResults.clear();
    }
    
    @FXML
    private void handleOpenDirectory() {
        try {
            File directory = new File(pathField.getText());
            if (directory.exists()) {
                java.awt.Desktop.getDesktop().open(directory);
            } else {
                showError("El directorio no existe: " + pathField.getText());
            }
        } catch (Exception e) {
            showError("No se pudo abrir el directorio: " + e.getMessage());
        }
    }
    
    private void exportGuests(String basePath) {
        try {
            List<Guest> guests = guestDAO.findAll();
            String filePath = basePath + "/guests.csv";
            
            CsvExporter.CsvExportResult result = CsvExporter.export(
                filePath,
                "id,name,document,phone,email,active",
                guests,
                guest -> String.format("%d,%s,%s,%s,%s,%s",
                    guest.getId(),
                    guest.getName(),
                    guest.getDocument(),
                    guest.getPhone(),
                    guest.getEmail(),
                    guest.isActive()
                )
            );
            
            exportResults.add(new ExportResult(
                "Huéspedes",
                guests.size(),
                "guests.csv",
                result.isSuccess() ? "Éxito" : "Error"
            ));
            
        } catch (Exception e) {
            exportResults.add(new ExportResult(
                "Huéspedes",
                0,
                "guests.csv",
                "Error: " + e.getMessage()
            ));
        }
    }
    
    private void exportRooms(String basePath) {
        try {
            List<Room> rooms = roomDAO.findAll();
            String filePath = basePath + "/rooms.csv";
            
            CsvExporter.CsvExportResult result = CsvExporter.export(
                filePath,
                "id,number,type,price,available",
                rooms,
                room -> String.format("%d,%d,%s,%.2f,%s",
                    room.getId(),
                    room.getNumber(),
                    room.getType(),
                    room.getPrice(),
                    room.isAvailable()
                )
            );
            
            exportResults.add(new ExportResult(
                "Habitaciones",
                rooms.size(),
                "rooms.csv",
                result.isSuccess() ? "Éxito" : "Error"
            ));
            
        } catch (Exception e) {
            exportResults.add(new ExportResult(
                "Habitaciones",
                0,
                "rooms.csv",
                "Error: " + e.getMessage()
            ));
        }
    }
    
    private void exportBookings(String basePath) {
        try {
            List<Booking> bookings = bookingDAO.findAll();
            String filePath = basePath + "/bookings.csv";
            
            CsvExporter.CsvExportResult result = CsvExporter.export(
                filePath,
                "id,guest_id,room_id,start_date,end_date,status",
                bookings,
                booking -> String.format("%d,%d,%d,%s,%s,%s",
                    booking.getId(),
                    booking.getGuestId(),
                    booking.getRoomId(),
                    booking.getStartDate(),
                    booking.getEndDate(),
                    booking.getStatus()
                )
            );
            
            exportResults.add(new ExportResult(
                "Reservas",
                bookings.size(),
                "bookings.csv",
                result.isSuccess() ? "Éxito" : "Error"
            ));
            
        } catch (Exception e) {
            exportResults.add(new ExportResult(
                "Reservas",
                0,
                "bookings.csv",
                "Error: " + e.getMessage()
            ));
        }
    }
    
    private void showError(String message) {
        exportErrorLabel.setText(message);
        exportErrorLabel.setVisible(true);
        exportErrorLabel.setManaged(true);
        exportSuccessLabel.setVisible(false);
        exportSuccessLabel.setManaged(false);
    }
    
    private void showSuccess(String message) {
        exportSuccessLabel.setText(message);
        exportSuccessLabel.setVisible(true);
        exportSuccessLabel.setManaged(true);
        exportErrorLabel.setVisible(false);
        exportErrorLabel.setManaged(false);
    }
    
    private void hideMessages() {
        exportErrorLabel.setVisible(false);
        exportErrorLabel.setManaged(false);
        exportSuccessLabel.setVisible(false);
        exportSuccessLabel.setManaged(false);
    }
    
    // Inner class for export results
    public static class ExportResult {
        private final String entity;
        private final int recordCount;
        private final String fileName;
        private final String status;
        
        public ExportResult(String entity, int recordCount, String fileName, String status) {
            this.entity = entity;
            this.recordCount = recordCount;
            this.fileName = fileName;
            this.status = status;
        }
        
        public String getEntity() { return entity; }
        public int getRecordCount() { return recordCount; }
        public String getFileName() { return fileName; }
        public String getStatus() { return status; }
    }
}
