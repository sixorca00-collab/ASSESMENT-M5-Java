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
    
    @FXML private Label sectionDescriptionLabel;
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

    public void configureForRole(String role) {
        if ("MANAGER".equalsIgnoreCase(role)) {
            sectionDescriptionLabel.setText("Download CSV snapshots to support planning and reporting.");
        } else {
            sectionDescriptionLabel.setText("Generate CSV snapshots for guests, rooms, and bookings.");
        }
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
        directoryChooser.setTitle("Select Export Directory");
        
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
                showError("Please select an export directory.");
                return;
            }
            
            boolean hasSelection = exportGuestsCheckBox.isSelected() || 
                                exportRoomsCheckBox.isSelected() || 
                                exportBookingsCheckBox.isSelected();
            
            if (!hasSelection) {
                showError("Select at least one dataset to export.");
                return;
            }
            
            if (exportGuestsCheckBox.isSelected()) {
                exportGuests(basePath);
            }
            
            if (exportRoomsCheckBox.isSelected()) {
                exportRooms(basePath);
            }
            
            if (exportBookingsCheckBox.isSelected()) {
                exportBookings(basePath);
            }
            
            resultsContainer.setVisible(true);
            resultsContainer.setManaged(true);
            
            showSuccess("Export complete. Review the detailed results below.");
            
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
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
                showError("Directory does not exist: " + pathField.getText());
            }
        } catch (Exception e) {
            showError("Unable to open the directory: " + e.getMessage());
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
                "Guests",
                guests.size(),
                "guests.csv",
                result.isSuccess() ? "Success" : "Error"
            ));
            
        } catch (Exception e) {
            exportResults.add(new ExportResult(
                "Guests",
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
                "Rooms",
                rooms.size(),
                "rooms.csv",
                result.isSuccess() ? "Success" : "Error"
            ));
            
        } catch (Exception e) {
            exportResults.add(new ExportResult(
                "Rooms",
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
                "Bookings",
                bookings.size(),
                "bookings.csv",
                result.isSuccess() ? "Success" : "Error"
            ));
            
        } catch (Exception e) {
            exportResults.add(new ExportResult(
                "Bookings",
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
