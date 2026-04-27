package org.hotelNova.views.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.hotelNova.models.Guest;
import org.hotelNova.services.GuestService;

import java.util.List;

public class GuestController {
    
    @FXML private VBox guestFormContainer;
    @FXML private TextField nameField;
    @FXML private TextField documentField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField searchField;
    @FXML private Label guestErrorLabel;
    @FXML private Label guestSuccessLabel;
    @FXML private TableView<Guest> guestsTable;
    @FXML private TableColumn<Guest, Integer> idColumn;
    @FXML private TableColumn<Guest, String> nameColumn;
    @FXML private TableColumn<Guest, String> documentColumn;
    @FXML private TableColumn<Guest, String> phoneColumn;
    @FXML private TableColumn<Guest, String> emailColumn;
    @FXML private TableColumn<Guest, Boolean> activeColumn;
    @FXML private TableColumn<Guest, Void> actionsColumn;
    
    private final GuestService guestService = new GuestService();
    private final ObservableList<Guest> guestList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupTable();
        loadGuests();
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        documentColumn.setCellValueFactory(new PropertyValueFactory<>("document"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setCellFactory(CheckBoxTableCell.forTableColumn(activeColumn));
        activeColumn.setEditable(false);
        
        setupActionsColumn();
        
        guestsTable.setItems(guestList);
        guestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Editar");
            private final Button toggleButton = new Button();
            private final HBox buttons = new HBox(5, editButton, toggleButton);
            
            {
                editButton.getStyleClass().add("button-secondary");
                toggleButton.getStyleClass().add("button-secondary");
                
                editButton.setOnAction(e -> editGuest(getTableView().getItems().get(getIndex())));
                toggleButton.setOnAction(e -> toggleGuestStatus(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Guest guest = getTableView().getItems().get(getIndex());
                    toggleButton.setText(guest.isActive() ? "Desactivar" : "Activar");
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void loadGuests() {
        try {
            List<Guest> guests = guestService.findAll();
            guestList.clear();
            guestList.addAll(guests);
        } catch (Exception e) {
            showError("Error al cargar huéspedes: " + e.getMessage());
        }
    }
    
    @FXML
    private void showAddGuestForm() {
        guestFormContainer.setVisible(true);
        guestFormContainer.setManaged(true);
        clearForm();
        hideMessages();
    }
    
    @FXML
    private void hideGuestForm() {
        guestFormContainer.setVisible(false);
        guestFormContainer.setManaged(false);
        clearForm();
        hideMessages();
    }
    
    @FXML
    private void handleSaveGuest() {
        try {
            validateForm();
            
            Guest guest = new Guest();
            guest.setName(nameField.getText().trim());
            guest.setDocument(documentField.getText().trim());
            guest.setPhone(phoneField.getText().trim());
            guest.setEmail(emailField.getText().trim());
            guest.setActive(true);
            
            guestService.create(guest);
            
            showSuccess("Huésped registrado exitosamente");
            hideGuestForm();
            loadGuests();
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadGuests();
            return;
        }
        
        try {
            List<Guest> allGuests = guestService.findAll();
            List<Guest> filtered = allGuests.stream()
                .filter(guest -> guest.getName().toLowerCase().contains(searchText) ||
                               guest.getDocument().toLowerCase().contains(searchText))
                .toList();
            
            guestList.clear();
            guestList.addAll(filtered);
            
        } catch (Exception e) {
            showError("Error al buscar huéspedes: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearSearch() {
        searchField.clear();
        loadGuests();
    }
    
    private void editGuest(Guest guest) {
        nameField.setText(guest.getName());
        documentField.setText(guest.getDocument());
        phoneField.setText(guest.getPhone());
        emailField.setText(guest.getEmail());
        
        showAddGuestForm();
    }
    
    private void toggleGuestStatus(Guest guest) {
        try {
            guest.setActive(!guest.isActive());
            guestService.update(guest);
            loadGuests();
            showSuccess("Estado del huésped actualizado");
        } catch (Exception e) {
            showError("Error al actualizar estado: " + e.getMessage());
        }
    }
    
    private void validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        if (documentField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("El documento es requerido");
        }
        if (phoneField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es requerido");
        }
        if (emailField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es requerido");
        }
    }
    
    private void clearForm() {
        nameField.clear();
        documentField.clear();
        phoneField.clear();
        emailField.clear();
    }
    
    private void showError(String message) {
        guestErrorLabel.setText(message);
        guestErrorLabel.setVisible(true);
        guestErrorLabel.setManaged(true);
        guestSuccessLabel.setVisible(false);
        guestSuccessLabel.setManaged(false);
    }
    
    private void showSuccess(String message) {
        guestSuccessLabel.setText(message);
        guestSuccessLabel.setVisible(true);
        guestSuccessLabel.setManaged(true);
        guestErrorLabel.setVisible(false);
        guestErrorLabel.setManaged(false);
    }
    
    private void hideMessages() {
        guestErrorLabel.setVisible(false);
        guestErrorLabel.setManaged(false);
        guestSuccessLabel.setVisible(false);
        guestSuccessLabel.setManaged(false);
    }
}
