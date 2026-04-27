package org.hotelNova;

import org.hotelNova.config.DbInit;
import org.hotelNova.DAO.BookingDAO;
import org.hotelNova.DAO.GuestDAO;
import org.hotelNova.DAO.RoomDAO;
import org.hotelNova.models.Booking;
import org.hotelNova.models.Guest;
import org.hotelNova.models.Room;
import org.hotelNova.services.BookingService;

import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        DbInit.init();

        RoomDAO roomDAO = new RoomDAO();
        GuestDAO guestDAO = new GuestDAO();
        BookingDAO bookingDAO = new BookingDAO();
        BookingService bookingService = new BookingService();

        // -------- CREATE ROOM --------
        Room room = new Room();
        room.setNumber((int) (Math.random() * 1000));
        room.setType("SINGLE");
        room.setPrice(150.0);
        room.setAvailable(true);

        roomDAO.create(room);

        // -------- CREATE GUEST --------
        Guest guest = new Guest();
        guest.setName("Test User");
        guest.setDocument("DOC" + System.currentTimeMillis());
        guest.setPhone("3000000000");
        guest.setEmail("test@mail.com");
        guest.setActive(true);

        guestDAO.create(guest);

        // -------- GET REAL DATA --------
        List<Room> rooms = roomDAO.findAll();
        List<Guest> guests = guestDAO.findAll();

        Room savedRoom = rooms.get(rooms.size() - 1);
        Guest savedGuest = guests.get(guests.size() - 1);

        System.out.println("Room ID: " + savedRoom.getId());
        System.out.println("Guest ID: " + savedGuest.getId());

        // -------- CREATE BOOKING --------
        LocalDate base = LocalDate.now().plusDays((int)(Math.random() * 10));

        Booking booking = new Booking();
        booking.setGuestId(savedGuest.getId());
        booking.setRoomId(savedRoom.getId());
        booking.setStartDate(base.toString());
        booking.setEndDate(base.plusDays(4).toString());
        booking.setStatus("RESERVED");

        try {
            bookingService.checkIn(booking);
            System.out.println("CHECK-IN SUCCESS");
        } catch (Exception e) {
            System.out.println("CHECK-IN ERROR: " + e.getMessage());
            return; // si falla, no seguimos
        }

        // -------- VERIFY ROOM STATE --------
        Room updatedRoom = roomDAO.findById(savedRoom.getId());
        System.out.println("Room available after check-in: " + updatedRoom.isAvailable());

        // -------- GET LAST BOOKING REAL --------
        List<Booking> bookings = bookingDAO.findAll();
        Booking lastBooking = bookings.get(bookings.size() - 1);

        int lastBookingId = lastBooking.getId();

        // -------- CHECKOUT --------
        try {
            double total = bookingService.checkOut(lastBookingId);
            System.out.println("CHECK-OUT SUCCESS | Total: " + total);
        } catch (Exception e) {
            System.out.println("CHECK-OUT ERROR: " + e.getMessage());
        }

        // -------- VERIFY ROOM RESET --------
        Room finalRoom = roomDAO.findById(savedRoom.getId());
        System.out.println("Room available after check-out: " + finalRoom.isAvailable());

        // -------- EXPORT IDENTITIES TO CSV --------
        System.out.println("\\n=== EXPORTING IDENTITIES TO CSV ===");
        
        // Export guests
        List<Guest> allGuests = guestDAO.findAll();
        org.hotelNova.utils.CsvExporter.CsvExportResult guestResult = org.hotelNova.utils.CsvExporter.export(
            "exports/guests.csv",
            "id,name,document,phone,email,active",
            allGuests,
            g -> String.format("%d,%s,%s,%s,%s,%s",
                g.getId(),
                g.getName(),
                g.getDocument(),
                g.getPhone(),
                g.getEmail(),
                g.isActive()
            )
        );
        System.out.println("Guests export: " + guestResult.toString());

        // Export rooms
        List<Room> allRooms = roomDAO.findAll();
        org.hotelNova.utils.CsvExporter.CsvExportResult roomResult = org.hotelNova.utils.CsvExporter.export(
            "exports/rooms.csv",
            "id,number,type,price,available",
            allRooms,
            r -> String.format("%d,%d,%s,%.2f,%s",
                r.getId(),
                r.getNumber(),
                r.getType(),
                r.getPrice(),
                r.isAvailable()
            )
        );
        System.out.println("Rooms export: " + roomResult.toString());

        // Export bookings
        List<Booking> allBookings = bookingDAO.findAll();
        org.hotelNova.utils.CsvExporter.CsvExportResult bookingResult = org.hotelNova.utils.CsvExporter.export(
            "exports/bookings.csv",
            "id,guest_id,room_id,start_date,end_date,status",
            allBookings,
            b -> String.format("%d,%d,%d,%s,%s,%s",
                b.getId(),
                b.getGuestId(),
                b.getRoomId(),
                b.getStartDate(),
                b.getEndDate(),
                b.getStatus()
            )
        );
        System.out.println("Bookings export: " + bookingResult.toString());
    }
}