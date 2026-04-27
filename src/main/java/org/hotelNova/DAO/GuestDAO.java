package org.hotelNova.DAO;

import org.hotelNova.models.Guest;
import org.hotelNova.utils.Helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class GuestDAO {

    public int create(Guest g) {
        return Helper.insert(
                "INSERT INTO huespedes (nombre, documento, telefono, email, activo) VALUES (?, ?, ?, ?, ?)",
                g.getName(),
                g.getDocument(),
                g.getPhone(),
                g.getEmail(),
                g.isActive() ? 1 : 0
        );
    }

    public int create(Connection conn, Guest g) {
        return Helper.insert(conn,
                "INSERT INTO huespedes (nombre, documento, telefono, email, activo) VALUES (?, ?, ?, ?, ?)",
                g.getName(),
                g.getDocument(),
                g.getPhone(),
                g.getEmail(),
                g.isActive() ? 1 : 0
        );
    }

    public Guest findById(int id) {
        return Helper.queryOne(
                "SELECT id, nombre AS name, documento AS document, telefono AS phone, email, activo AS active FROM huespedes WHERE id = ?",
                this::map,
                id
        );
    }

    public List<Guest> findAll() {
        return Helper.query(
                "SELECT id, nombre AS name, documento AS document, telefono AS phone, email, activo AS active FROM huespedes WHERE activo = 1",
                this::map
        );
    }

    public Guest findByDocument(String document) {
        return Helper.queryOne(
                "SELECT id, nombre AS name, documento AS document, telefono AS phone, email, activo AS active FROM huespedes WHERE documento = ?",
                this::map,
                document
        );
    }

    public void update(Guest g) {
        Helper.update(
                "UPDATE huespedes SET nombre=?, documento=?, telefono=?, email=?, activo=? WHERE id=?",
                g.getName(),
                g.getDocument(),
                g.getPhone(),
                g.getEmail(),
                g.isActive() ? 1 : 0,
                g.getId()
        );
    }

    public void delete(int id) {
        Helper.update(
                "UPDATE huespedes SET activo = 0 WHERE id = ?",
                id
        );
    }

    private Guest map(ResultSet rs) {
        try {
            Guest g = new Guest();
            g.setId(rs.getInt("id"));
            g.setName(rs.getString("name"));
            g.setDocument(rs.getString("document"));
            g.setPhone(rs.getString("phone"));
            g.setEmail(rs.getString("email"));
            g.setActive(rs.getInt("active") == 1);
            return g;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
