package org.hotelNova.services;

import org.hotelNova.DAO.GuestDAO;
import org.hotelNova.models.Guest;

import java.util.List;

public class GuestService {

    private final GuestDAO guestDAO = new GuestDAO();

    public void create(Guest guest) {
        if (guest.getName() == null || guest.getName().isEmpty()) {
            throw new RuntimeException("Name is required");
        }

        guest.setActive(true);
        guestDAO.create(guest);
    }

    public List<Guest> findAll() {
        return guestDAO.findAll();
    }

    public Guest findById(int id) {
        return guestDAO.findById(id);
    }

    public void update(Guest guest) {
        guestDAO.update(guest);
    }

    public void deactivate(int id) {
        Guest g = guestDAO.findById(id);

        if (g == null) {
            throw new RuntimeException("Guest not found");
        }

        g.setActive(false);
        guestDAO.update(g);
    }
}