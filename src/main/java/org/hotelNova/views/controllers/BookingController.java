package org.hotelNova.views.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.hotelNova.models.Booking;
import org.hotelNova.models.Guest;
import org.hotelNova.models.Room;
import org.hotelNova.services.BookingService;
import org.hotelNova.services.GuestService;
import org.hotelNova.services.RoomService;

import java.time.LocalDate;
import java.util.List;

public class BookingController {
    
    @FXML private VBox bookingFormContainer;
    @FXML private ComboBox<Guest> guestComboBox;
    @FXML private ComboBox<Room> roomComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private Label bookingErrorLabel;
    @FXML private Label bookingSuccessLabel;
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> bookingIdColumn;
    @FXML private TableColumn<Booking, String> bookingGuestColumn;
    @FXML private TableColumn<Booking, String> bookingRoomColumn;
    @FXML private TableColumn<Booking, String> bookingStartDateColumn;
    @FXML private TableColumn<Booking, String> bookingEndDateColumn;
    @FXML private TableColumn<Booking, String> bookingStatusColumn;
    @FXML private TableColumn<Booking, Void> bookingActionsColumn;
    
    private final BookingService bookingService = new BookingService();
    private final GuestService guestService = new GuestService();
    private final RoomService roomService = new RoomService();
    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    
    private static final String[] BOOKING_STATUSES = {"Todos", "RESERVED", "OCCUPIED", "FINISHED"};
    
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTable();
        loadBookings();
    }
    
    private void setupComboBoxes() {
        filterStatusComboBox.getItems().addAll(BOOKING_STATUSES);
        filterStatusComboBox.setValue("Todos");
        
        loadGuests();
        loadAvailableRooms();
    }
    
    private void loadGuests() {
        try {
            List<Guest> guests = guestService.findAll();
            guestComboBox.getItems().clear();
            guestComboBox.getItems().addAll(guests);
        } catch (Exception e) {
            System.err.println("Error loading guests: " + e.getMessage());
        }
    }
    
    private void loadAvailableRooms() {
        try {
            List<Room> rooms = roomService.findAll().stream()
                .filter(Room::isAvailable)
                .toList();
            roomComboBox.getItems().clear();
            roomComboBox.getItems().addAll(rooms);
        } catch (Exception e) {
            System.err.println("Error loading rooms: " + e.getMessage());
        }
    }
    
    private void setupTable() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookingGuestColumn.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        bookingRoomColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        bookingStartDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        bookingEndDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        setupActionsColumn();
        
        bookingsTable.setItems(bookingList);
        bookingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void setupActionsColumn() {
        bookingActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button checkoutButton = new Button("Check-Out");
            private final HBox buttons = new HBox(5, checkoutButton);
            
            {
                checkoutButton.getStyleClass().add("button-primary");
                checkoutButton.setOnAction(e -> handleCheckout(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking booking = getTableView().getItems().get(getIndex());
                    checkoutButton.setDisable(!"OCCUPIED".equals(booking.getStatus()));
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void loadBookings() {
        try {
            List<Booking> bookings = bookingService.findAll();
            bookingList.clear();
            bookingList.addAll(bookings);
        } catch (Exception e) {
            showError("Error al cargar reservas: " + e.getMessage());
        }
    }
    
    @FXML
    private void showAddBookingForm() {
        bookingFormContainer.setVisible(true);
        bookingFormContainer.setManaged(true);
        clearForm();
        hideMessages();
        loadGuests();
        loadAvailableRooms();
    }
    
    @FXML
    private void hideBookingForm() {
        bookingFormContainer.setVisible(false);
        bookingFormContainer.setManaged(false);
        clearForm();
        hideMessages();
    }
    
    @FXML
    private void handleCheckIn() {
        try {
            validateForm();
            
            Guest selectedGuest = guestComboBox.getValue();
            Room selectedRoom = roomComboBox.getValue();
            
            Booking booking = new Booking();
            booking.setGuestId(selectedGuest.getId());
            booking.setRoomId(selectedRoom.getId());
            booking.setStartDate(startDatePicker.getValue().toString());
            booking.setEndDate(endDatePicker.getValue().toString());
            booking.setStatus("RESERVED");
            
            bookingService.checkIn(booking);
            
            showSuccess("Reserva creada exitosamente");
            hideBookingForm();
            loadBookings();
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleFilter() {
        String statusFilter = filterStatusComboBox.getValue();
        
        try {
            List<Booking> allBookings = bookingService.findAll();
            List<Booking> filtered = allBookings.stream()
                .filter(booking -> "Todos".equals(statusFilter) || statusFilter.equals(booking.getStatus()))
                .toList();
            
            bookingList.clear();
            bookingList.addAll(filtered);
            
        } catch (Exception e) {
            showError("Error al filtrar reservas: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearFilter() {
        filterStatusComboBox.setValue("Todos");
        loadBookings();
    }
    
    private void handleCheckout(Booking booking) {
        try {
            double total = bookingService.checkOut(booking.getId());
            showSuccess("Check-Out completado. Total: $" + String.format("%.2f", total));
            loadBookings();
        } catch (Exception e) {
            showError("Error en check-out: " + e.getMessage());
        }
    }
    
    private void validateForm() {
        if (guestComboBox.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar un huésped");
        }
        if (roomComboBox.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar una habitación");
        }
        if (startDatePicker.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar fecha de inicio");
        }
        if (endDatePicker.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar fecha de fin");
        }
        
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        
        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser anterior a hoy");
        }
        
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
    }
    
    private void clearForm() {
        guestComboBox.setValue(null);
        roomComboBox.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }
    
    private void showError(String message) {
        bookingErrorLabel.setText(message);
        bookingErrorLabel.setVisible(true);
        bookingErrorLabel.setManaged(true);
        bookingSuccessLabel.setVisible(false);
        bookingSuccessLabel.setManaged(false);
    }
    
    private void showSuccess(String message) {
        bookingSuccessLabel.setText(message);
        bookingSuccessLabel.setVisible(true);
        bookingSuccessLabel.setManaged(true);
        bookingErrorLabel.setVisible(false);
        bookingErrorLabel.setManaged(false);
    }
    
    private void hideMessages() {
        bookingErrorLabel.setVisible(false);
        bookingErrorLabel.setManaged(false);
        bookingSuccessLabel.setVisible(false);
        bookingSuccessLabel.setManaged(false);
    }
}
