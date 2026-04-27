package org.hotelNova.controllers;

import org.hotelNova.models.Booking;
import org.hotelNova.services.BookingService;
import org.hotelNova.services.LogService;

public class BookingController {

    private final BookingService bookingService = new BookingService();
    private final LogService logService = new LogService();

    public void createBooking(Booking booking) {
        try {
            bookingService.checkIn(booking);

            logService.log("POST", "/bookings", "Booking created");

            System.out.println("201 CREATED");

        } catch (Exception e) {
            logService.log("POST", "/bookings", e.getMessage());
            System.out.println("400 ERROR: " + e.getMessage());
        }
    }

    public void checkOut(int bookingId) {
        try {
            double total = bookingService.checkOut(bookingId);

            logService.log("POST", "/checkout", "Checkout success");

            System.out.println("200 OK - Total: " + total);

        } catch (Exception e) {
            logService.log("POST", "/checkout", e.getMessage());
            System.out.println("400 ERROR: " + e.getMessage());
        }
    }
}