package org.hotelNova.DAO;

import org.hotelNova.models.Room;
import org.hotelNova.utils.Helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class RoomDAO {

    public void create(Room r) {
        Helper.update(
                "INSERT INTO habitaciones (numero, tipo, precio_noche, activo) VALUES (?, ?, ?, ?)",
                r.getNumber(),
                r.getType(),
                r.getPrice(),
                r.isAvailable() ? 1 : 0
        );
    }

    public Room findById(int id) {
        return Helper.queryOne(
                "SELECT id, numero AS number, tipo AS type, precio_noche AS price, activo AS available FROM habitaciones WHERE id = ?",
                this::map,
                id
        );
    }

    public List<Room> findAll() {
        return Helper.query(
                "SELECT id, numero AS number, tipo AS type, precio_noche AS price, activo AS available FROM habitaciones",
                this::map
        );
    }

    public void update(Room r) {
        Helper.update(
                "UPDATE habitaciones SET numero=?, tipo=?, precio_noche=?, activo=? WHERE id=?",
                r.getNumber(),
                r.getType(),
                r.getPrice(),
                r.isAvailable() ? 1 : 0,
                r.getId()
        );
    }

    public void update(Connection conn, Room r) {
        Helper.update(conn,
                "UPDATE habitaciones SET numero=?, tipo=?, precio_noche=?, activo=? WHERE id=?",
                r.getNumber(),
                r.getType(),
                r.getPrice(),
                r.isAvailable() ? 1 : 0,
                r.getId()
        );
    }

    public void delete(int id) {
        Helper.update(
                "UPDATE habitaciones SET activo = 0 WHERE id = ?",
                id
        );
    }

    private Room map(ResultSet rs) {
        try {
            Room r = new Room();
            r.setId(rs.getInt("id"));
            r.setNumber(rs.getInt("number"));
            r.setType(rs.getString("type"));
            r.setPrice(rs.getDouble("price"));
            r.setAvailable(rs.getInt("available") == 1);
            return r;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}