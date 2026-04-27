package org.hotelNova.DAO;

import org.hotelNova.models.Booking;
import org.hotelNova.utils.Helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class BookingDAO {

    public void create(Booking b) {
        Helper.update(
                "INSERT INTO reservas (huesped_id, habitacion_id, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)",
                b.getGuestId(),
                b.getRoomId(),
                b.getStartDate(),
                b.getEndDate(),
                b.getStatus()
        );
    }

    public Booking findById(int id) {
        return Helper.queryOne(
                "SELECT id, huesped_id AS guestId, habitacion_id AS roomId, fecha_inicio AS startDate, fecha_fin AS endDate, estado AS status FROM reservas WHERE id = ?",
                this::map, id
        );
    }

    public List<Booking> findAll() {
        return Helper.query(
                "SELECT id, huesped_id AS guestId, habitacion_id AS roomId, fecha_inicio AS startDate, fecha_fin AS endDate, estado AS status FROM reservas",
                this::map
        );
    }

    public List<Booking> findByGuestId(int guestId) {
        return Helper.query(
                "SELECT id, huesped_id AS guestId, habitacion_id AS roomId, fecha_inicio AS startDate, fecha_fin AS endDate, estado AS status FROM reservas WHERE huesped_id = ?",
                this::map,
                guestId
        );
    }

    public void update(Booking b) {
        Helper.update(
                "UPDATE reservas SET huesped_id=?, habitacion_id=?, fecha_inicio=?, fecha_fin=?, estado=? WHERE id=?",
                b.getGuestId(),
                b.getRoomId(),
                b.getStartDate(),
                b.getEndDate(),
                b.getStatus(),
                b.getId()
        );
    }

    public void delete(int id) {
        Helper.update(
                "DELETE FROM reservas WHERE id = ?", id
        );
    }

    public void create(Connection conn, Booking b) {
        Helper.update(conn,
                "INSERT INTO reservas (huesped_id, habitacion_id, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)",
                b.getGuestId(),
                b.getRoomId(),
                b.getStartDate(),
                b.getEndDate(),
                b.getStatus()
        );
    }

    public void updateStatus(Connection conn, int bookingId, String status) {
        Helper.update(conn,
                "UPDATE reservas SET estado = ? WHERE id = ?",
                status,
                bookingId
        );
    }


    private Booking map(ResultSet rs) {
        try {
            Booking b = new Booking();
            b.setId(rs.getInt("id"));
            b.setGuestId(rs.getInt("guestId"));
            b.setRoomId(rs.getInt("roomId"));
            b.setStartDate(rs.getString("startDate"));
            b.setEndDate(rs.getString("endDate"));
            b.setStatus(rs.getString("status"));
            return b;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
