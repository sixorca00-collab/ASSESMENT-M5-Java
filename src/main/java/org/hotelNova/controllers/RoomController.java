package org.hotelNova.controllers;

import org.hotelNova.models.Room;
import org.hotelNova.services.RoomService;

import java.util.List;

public class RoomController {

    private final RoomService roomService = new RoomService();

    public void create(Room room) {
        try {
            roomService.create(room);
            System.out.println("201 CREATED");
        } catch (Exception e) {
            System.out.println("400 ERROR: " + e.getMessage());
        }
    }

    public void getAll() {
        List<Room> rooms = roomService.findAll();

        System.out.println("200 OK");
        for (Room r : rooms) {
            System.out.println(r.getId() + " - " + r.getNumber());
        }
    }
}