package org.hotelNova.views.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.hotelNova.models.Room;
import org.hotelNova.services.RoomService;

import java.util.List;

public class RoomController {
    
    @FXML private Label sectionDescriptionLabel;
    @FXML private Label modeInfoLabel;
    @FXML private Label formTitleLabel;
    @FXML private Button addRoomButton;
    @FXML private VBox roomFormContainer;
    @FXML private TextField numberField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private Label roomErrorLabel;
    @FXML private Label roomSuccessLabel;
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, Integer> roomIdColumn;
    @FXML private TableColumn<Room, Integer> roomNumberColumn;
    @FXML private TableColumn<Room, String> roomTypeColumn;
    @FXML private TableColumn<Room, Double> roomPriceColumn;
    @FXML private TableColumn<Room, Boolean> roomAvailableColumn;
    @FXML private TableColumn<Room, Void> roomActionsColumn;
    
    private final RoomService roomService = new RoomService();
    private final ObservableList<Room> roomList = FXCollections.observableArrayList();
    
    private static final String[] ROOM_TYPES = {"SINGLE", "DOUBLE", "SUITE", "DELUXE"};
    private Room editingRoom;
    private String currentRole = "ADMIN";
    private boolean readOnly;
    
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTable();
        loadRooms();
    }

    public void configureForRole(String role) {
        currentRole = role == null ? "ADMIN" : role.toUpperCase();
        readOnly = "RECEPTIONIST".equals(currentRole) || "GUEST".equals(currentRole);

        if ("ADMIN".equals(currentRole)) {
            sectionDescriptionLabel.setText("Manage room inventory, pricing, and availability states.");
        } else if ("MANAGER".equals(currentRole)) {
            sectionDescriptionLabel.setText("Oversee inventory and pricing while monitoring room status.");
        } else if ("RECEPTIONIST".equals(currentRole)) {
            sectionDescriptionLabel.setText("Room availability is visible to support front-desk assignments.");
        } else {
            sectionDescriptionLabel.setText("Browse currently available room categories and rates.");
        }

        addRoomButton.setVisible(!readOnly);
        addRoomButton.setManaged(!readOnly);
        roomActionsColumn.setVisible(!readOnly);
        if (readOnly) {
            hideRoomForm();
            showModeInfo("This section is read-only for your role.");
        } else {
            hideModeInfo();
        }
        loadRooms();
    }
    
    private void setupComboBoxes() {
        typeComboBox.getItems().addAll(ROOM_TYPES);
        filterTypeComboBox.getItems().add("All");
        filterTypeComboBox.getItems().addAll(ROOM_TYPES);
        filterStatusComboBox.getItems().addAll("All", "Available", "Occupied");
        
        filterTypeComboBox.setValue("All");
        filterStatusComboBox.setValue("All");
    }
    
    private void setupTable() {
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        roomPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        roomAvailableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
        roomAvailableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(roomAvailableColumn));
        roomAvailableColumn.setEditable(false);
        
        setupActionsColumn();
        
        roomsTable.setItems(roomList);
        roomsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void setupActionsColumn() {
        roomActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button toggleButton = new Button();
            private final HBox buttons = new HBox(5, editButton, toggleButton);
            
            {
                editButton.getStyleClass().add("button-secondary");
                toggleButton.getStyleClass().add("button-secondary");
                
                editButton.setOnAction(e -> editRoom(getTableView().getItems().get(getIndex())));
                toggleButton.setOnAction(e -> toggleRoomStatus(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Room room = getTableView().getItems().get(getIndex());
                    toggleButton.setText(room.isAvailable() ? "Mark Occupied" : "Mark Available");
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void loadRooms() {
        try {
            List<Room> rooms = roomService.findAll();
            if ("GUEST".equals(currentRole)) {
                rooms = rooms.stream().filter(Room::isAvailable).toList();
            }
            roomList.clear();
            roomList.addAll(rooms);
        } catch (Exception e) {
            showError("Unable to load rooms: " + e.getMessage());
        }
    }
    
    @FXML
    private void showAddRoomForm() {
        if (readOnly) {
            showModeInfo("This section is read-only for your role.");
            return;
        }

        roomFormContainer.setVisible(true);
        roomFormContainer.setManaged(true);
        hideMessages();
        hideModeInfo();
        if (editingRoom == null) {
            formTitleLabel.setText("Add Room");
            clearFormFields();
        }
    }
    
    @FXML
    private void hideRoomForm() {
        roomFormContainer.setVisible(false);
        roomFormContainer.setManaged(false);
        clearForm();
        hideMessages();
    }
    
    @FXML
    private void handleSaveRoom() {
        try {
            validateForm();

            boolean updating = editingRoom != null;
            Room room = updating ? editingRoom : new Room();
            room.setNumber(Integer.parseInt(numberField.getText().trim()));
            room.setType(typeComboBox.getValue());
            room.setPrice(Double.parseDouble(priceField.getText().trim()));
            if (!updating) {
                room.setAvailable(true);
                roomService.create(room);
                hideRoomForm();
                showSuccess("Room registered successfully.");
            } else {
                roomService.update(room);
                hideRoomForm();
                showSuccess("Room updated successfully.");
            }
            loadRooms();
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleFilter() {
        String typeFilter = filterTypeComboBox.getValue();
        String statusFilter = filterStatusComboBox.getValue();
        
        try {
            List<Room> allRooms = roomService.findAll();
            if ("GUEST".equals(currentRole)) {
                allRooms = allRooms.stream().filter(Room::isAvailable).toList();
            }
            List<Room> filtered = allRooms.stream()
                .filter(room -> {
                    boolean typeMatch = "All".equals(typeFilter) || typeFilter.equals(room.getType());
                    boolean statusMatch = "All".equals(statusFilter) || 
                        ("Available".equals(statusFilter) && room.isAvailable()) ||
                        ("Occupied".equals(statusFilter) && !room.isAvailable());
                    return typeMatch && statusMatch;
                })
                .toList();
            
            roomList.clear();
            roomList.addAll(filtered);
            
        } catch (Exception e) {
            showError("Unable to filter rooms: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearFilter() {
        filterTypeComboBox.setValue("All");
        filterStatusComboBox.setValue("GUEST".equals(currentRole) ? "Available" : "All");
        loadRooms();
    }
    
    private void editRoom(Room room) {
        editingRoom = room;
        formTitleLabel.setText("Edit Room");
        showAddRoomForm();
        numberField.setText(String.valueOf(room.getNumber()));
        typeComboBox.setValue(room.getType());
        priceField.setText(String.valueOf(room.getPrice()));
    }
    
    private void toggleRoomStatus(Room room) {
        try {
            room.setAvailable(!room.isAvailable());
            roomService.update(room);
            loadRooms();
            showSuccess("Room availability updated.");
        } catch (Exception e) {
            showError("Unable to update room availability: " + e.getMessage());
        }
    }
    
    private void validateForm() {
        if (numberField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Room number is required.");
        }
        if (typeComboBox.getValue() == null) {
            throw new IllegalArgumentException("Room type is required.");
        }
        if (priceField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Nightly rate is required.");
        }
        
        try {
            Integer.parseInt(numberField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Room number must be numeric.");
        }
        
        try {
            Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Nightly rate must be numeric.");
        }
    }
    
    private void clearForm() {
        editingRoom = null;
        formTitleLabel.setText("Add Room");
        clearFormFields();
    }

    private void clearFormFields() {
        numberField.clear();
        typeComboBox.setValue(null);
        priceField.clear();
    }
    
    private void showError(String message) {
        roomErrorLabel.setText(message);
        roomErrorLabel.setVisible(true);
        roomErrorLabel.setManaged(true);
        roomSuccessLabel.setVisible(false);
        roomSuccessLabel.setManaged(false);
    }
    
    private void showSuccess(String message) {
        roomSuccessLabel.setText(message);
        roomSuccessLabel.setVisible(true);
        roomSuccessLabel.setManaged(true);
        roomErrorLabel.setVisible(false);
        roomErrorLabel.setManaged(false);
    }
    
    private void hideMessages() {
        roomErrorLabel.setVisible(false);
        roomErrorLabel.setManaged(false);
        roomSuccessLabel.setVisible(false);
        roomSuccessLabel.setManaged(false);
    }

    private void showModeInfo(String message) {
        modeInfoLabel.setText(message);
        modeInfoLabel.setVisible(true);
        modeInfoLabel.setManaged(true);
    }

    private void hideModeInfo() {
        modeInfoLabel.setVisible(false);
        modeInfoLabel.setManaged(false);
    }
}
