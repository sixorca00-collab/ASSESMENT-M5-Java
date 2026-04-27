package org.hotelNova.services;

import org.hotelnova.config.AppConfig;
import org.hotelNova.DAO.BookingDAO;
import org.hotelNova.DAO.GuestDAO;
import org.hotelNova.DAO.RoomDAO;
import org.hotelNova.models.Booking;
import org.hotelNova.models.Guest;
import org.hotelNova.models.Room;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final GuestDAO guestDAO = new GuestDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    public void checkIn(Booking booking) {

        Connection conn = null;

        try {
            conn = AppConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            validateDates(booking);
            validateGuest(booking.getGuestId());
            Room room = validateRoom(booking.getRoomId());
            validateAvailability(booking);

            booking.setStatus("OCCUPIED");

            bookingDAO.create(conn, booking);

            room.setAvailable(false);
            roomDAO.update(conn, room);

            conn.commit();

        } catch (Exception e) {

            try {
                if (conn != null) conn.rollback();
            } catch (Exception ignored) {}

            e.printStackTrace();
            throw new RuntimeException("Check-in failed", e);

        } finally {
            try {
                if (conn != null) conn.close();
            } catch (Exception ignored) {}
        }
    }

    public double checkOut(int bookingId) {

        Connection conn = null;

        try {
            conn = AppConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            Booking booking = bookingDAO.findById(bookingId);

            if (booking == null) {
                throw new RuntimeException("Booking not found");
            }

            if (!booking.getStatus().equals("OCCUPIED")) {
                throw new RuntimeException("Booking not active");
            }

            Room room = roomDAO.findById(booking.getRoomId());

            if (room == null) {
                throw new RuntimeException("Room not found");
            }

            long nights = ChronoUnit.DAYS.between(
                    LocalDate.parse(booking.getStartDate()),
                    LocalDate.parse(booking.getEndDate())
            );

            double iva = 0.19;
            double total = nights * room.getPrice() * (1 + iva);

            bookingDAO.updateStatus(conn, bookingId, "FINISHED");

            room.setAvailable(true);
            roomDAO.update(conn, room);

            conn.commit();

            return total;

        } catch (Exception e) {

            try {
                if (conn != null) conn.rollback();
            } catch (Exception ignored) {}

            e.printStackTrace();
            throw new RuntimeException("Check-out failed", e);

        } finally {
            try {
                if (conn != null) conn.close();
            } catch (Exception ignored) {}
        }
    }

    private void validateDates(Booking b) {
        LocalDate start = LocalDate.parse(b.getStartDate());
        LocalDate end = LocalDate.parse(b.getEndDate());

        if (!start.isBefore(end)) {
            throw new RuntimeException("Invalid dates");
        }
    }

    private void validateGuest(int guestId) {
        Guest g = guestDAO.findById(guestId);

        if (g == null) {
            throw new RuntimeException("Guest not found");
        }

        if (!g.isActive()) {
            throw new RuntimeException("Guest inactive");
        }
    }

    private Room validateRoom(int roomId) {
        Room r = roomDAO.findById(roomId);

        if (r == null) {
            throw new RuntimeException("Room not found");
        }

        if (!r.isAvailable()) {
            throw new RuntimeException("Room not available");
        }

        return r;
    }

    private void validateAvailability(Booking booking) {

        List<Booking> bookings = bookingDAO.findAll();

        LocalDate start = LocalDate.parse(booking.getStartDate());
        LocalDate end = LocalDate.parse(booking.getEndDate());

        for (Booking b : bookings) {

            if (b.getRoomId() == booking.getRoomId()
                    && !b.getStatus().equals("FINISHED")) {

                LocalDate existingStart = LocalDate.parse(b.getStartDate());
                LocalDate existingEnd = LocalDate.parse(b.getEndDate());

                boolean overlap =
                        start.isBefore(existingEnd) &&
                                end.isAfter(existingStart);

                if (overlap) {
                    throw new RuntimeException("Room already booked");
                }
            }
        }
    }
    
    public List<Booking> findAll() {
        return bookingDAO.findAll();
    }

    public List<Booking> findByGuestId(int guestId) {
        return bookingDAO.findByGuestId(guestId);
    }
}
