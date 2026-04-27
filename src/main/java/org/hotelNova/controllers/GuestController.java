package org.hotelNova.controllers;

import org.hotelNova.models.Guest;
import org.hotelNova.services.GuestService;

import java.util.List;

public class GuestController {

    private final GuestService guestService = new GuestService();

    public void create(Guest guest) {
        try {
            guestService.create(guest);
            System.out.println("201 CREATED");
        } catch (Exception e) {
            System.out.println("400 ERROR: " + e.getMessage());
        }
    }

    public void getAll() {
        List<Guest> guests = guestService.findAll();

        System.out.println("200 OK");
        for (Guest g : guests) {
            System.out.println(g.getId() + " - " + g.getName());
        }
    }
}