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
    
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTable();
        loadRooms();
    }
    
    private void setupComboBoxes() {
        typeComboBox.getItems().addAll(ROOM_TYPES);
        filterTypeComboBox.getItems().add("Todos");
        filterTypeComboBox.getItems().addAll(ROOM_TYPES);
        filterStatusComboBox.getItems().addAll("Todos", "Disponible", "Ocupado");
        
        filterTypeComboBox.setValue("Todos");
        filterStatusComboBox.setValue("Todos");
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
            private final Button editButton = new Button("Editar");
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
                    toggleButton.setText(room.isAvailable() ? "Ocupar" : "Liberar");
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void loadRooms() {
        try {
            List<Room> rooms = roomService.findAll();
            roomList.clear();
            roomList.addAll(rooms);
        } catch (Exception e) {
            showError("Error al cargar habitaciones: " + e.getMessage());
        }
    }
    
    @FXML
    private void showAddRoomForm() {
        roomFormContainer.setVisible(true);
        roomFormContainer.setManaged(true);
        clearForm();
        hideMessages();
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
            
            Room room = new Room();
            room.setNumber(Integer.parseInt(numberField.getText().trim()));
            room.setType(typeComboBox.getValue());
            room.setPrice(Double.parseDouble(priceField.getText().trim()));
            room.setAvailable(true);
            
            roomService.create(room);
            
            showSuccess("Habitación registrada exitosamente");
            hideRoomForm();
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
            List<Room> filtered = allRooms.stream()
                .filter(room -> {
                    boolean typeMatch = "Todos".equals(typeFilter) || typeFilter.equals(room.getType());
                    boolean statusMatch = "Todos".equals(statusFilter) || 
                        ("Disponible".equals(statusFilter) && room.isAvailable()) ||
                        ("Ocupado".equals(statusFilter) && !room.isAvailable());
                    return typeMatch && statusMatch;
                })
                .toList();
            
            roomList.clear();
            roomList.addAll(filtered);
            
        } catch (Exception e) {
            showError("Error al filtrar habitaciones: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearFilter() {
        filterTypeComboBox.setValue("Todos");
        filterStatusComboBox.setValue("Todos");
        loadRooms();
    }
    
    private void editRoom(Room room) {
        numberField.setText(String.valueOf(room.getNumber()));
        typeComboBox.setValue(room.getType());
        priceField.setText(String.valueOf(room.getPrice()));
        
        showAddRoomForm();
    }
    
    private void toggleRoomStatus(Room room) {
        try {
            room.setAvailable(!room.isAvailable());
            roomService.update(room);
            loadRooms();
            showSuccess("Estado de la habitación actualizado");
        } catch (Exception e) {
            showError("Error al actualizar estado: " + e.getMessage());
        }
    }
    
    private void validateForm() {
        if (numberField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de habitación es requerido");
        }
        if (typeComboBox.getValue() == null) {
            throw new IllegalArgumentException("El tipo de habitación es requerido");
        }
        if (priceField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("El precio es requerido");
        }
        
        try {
            Integer.parseInt(numberField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El número de habitación debe ser válido");
        }
        
        try {
            Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser válido");
        }
    }
    
    private void clearForm() {
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
}
