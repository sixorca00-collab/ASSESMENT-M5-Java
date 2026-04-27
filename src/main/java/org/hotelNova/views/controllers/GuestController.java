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
    
    @FXML private Label sectionDescriptionLabel;
    @FXML private Label modeInfoLabel;
    @FXML private Label formTitleLabel;
    @FXML private Button addGuestButton;
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
    private Guest editingGuest;
    private boolean readOnly;
    
    @FXML
    public void initialize() {
        setupTable();
        loadGuests();
    }

    public void configureForRole(String role) {
        String normalizedRole = role == null ? "ADMIN" : role.toUpperCase();
        readOnly = "MANAGER".equals(normalizedRole) || "GUEST".equals(normalizedRole);

        if ("RECEPTIONIST".equals(normalizedRole)) {
            sectionDescriptionLabel.setText("Register new guests, update contact details, and activate or deactivate profiles.");
        } else if ("ADMIN".equals(normalizedRole)) {
            sectionDescriptionLabel.setText("Maintain the complete guest directory and control guest record status.");
        } else {
            sectionDescriptionLabel.setText("Guest records are visible here for reference only.");
        }

        addGuestButton.setVisible(!readOnly);
        addGuestButton.setManaged(!readOnly);
        actionsColumn.setVisible(!readOnly);
        if (readOnly) {
            hideGuestForm();
            showModeInfo("This section is read-only for your role.");
        } else {
            hideModeInfo();
        }
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
            private final Button editButton = new Button("Edit");
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
                    toggleButton.setText(guest.isActive() ? "Deactivate" : "Activate");
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
            showError("Unable to load guests: " + e.getMessage());
        }
    }
    
    @FXML
    private void showAddGuestForm() {
        if (readOnly) {
            showModeInfo("This section is read-only for your role.");
            return;
        }

        guestFormContainer.setVisible(true);
        guestFormContainer.setManaged(true);
        hideMessages();
        hideModeInfo();
        if (editingGuest == null) {
            formTitleLabel.setText("Register Guest");
            clearFormFields();
        }
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

            boolean updating = editingGuest != null;
            Guest guest = updating ? editingGuest : new Guest();
            guest.setName(nameField.getText().trim());
            guest.setDocument(documentField.getText().trim());
            guest.setPhone(phoneField.getText().trim());
            guest.setEmail(emailField.getText().trim());
            if (!updating) {
                guest.setActive(true);
                guestService.create(guest);
                hideGuestForm();
                showSuccess("Guest registered successfully.");
            } else {
                guestService.update(guest);
                hideGuestForm();
                showSuccess("Guest updated successfully.");
            }
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
            showError("Unable to search guests: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearSearch() {
        searchField.clear();
        loadGuests();
    }
    
    private void editGuest(Guest guest) {
        editingGuest = guest;
        formTitleLabel.setText("Edit Guest");
        showAddGuestForm();
        nameField.setText(guest.getName());
        documentField.setText(guest.getDocument());
        phoneField.setText(guest.getPhone());
        emailField.setText(guest.getEmail());
    }
    
    private void toggleGuestStatus(Guest guest) {
        try {
            guest.setActive(!guest.isActive());
            guestService.update(guest);
            loadGuests();
            showSuccess("Guest status updated.");
        } catch (Exception e) {
            showError("Unable to update guest status: " + e.getMessage());
        }
    }
    
    private void validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Guest name is required.");
        }
        if (documentField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Document ID is required.");
        }
        if (phoneField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        if (emailField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
    }
    
    private void clearForm() {
        editingGuest = null;
        formTitleLabel.setText("Register Guest");
        clearFormFields();
    }

    private void clearFormFields() {
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
