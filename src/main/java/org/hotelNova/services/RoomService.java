package org.hotelNova.services;

import org.hotelNova.DAO.RoomDAO;
import org.hotelNova.models.Room;

import java.util.List;

public class RoomService {

    private final RoomDAO roomDAO = new RoomDAO();

    public void create(Room room) {
        if (room.getNumber() <= 0) {
            throw new RuntimeException("Invalid room number");
        }

        roomDAO.create(room);
    }

    public List<Room> findAll() {
        return roomDAO.findAll();
    }

    public Room findById(int id) {
        return roomDAO.findById(id);
    }

    public void update(Room room) {
        roomDAO.update(room);
    }

    public void delete(int id) {
        roomDAO.delete(id);
    }

    public void setAvailability(int roomId, boolean available) {
        Room room = roomDAO.findById(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        room.setAvailable(available);
        roomDAO.update(room);
    }
}