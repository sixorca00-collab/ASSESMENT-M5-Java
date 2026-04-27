package org.hotelNova.DAO;

import org.hotelnova.config.AppConfig;
import org.hotelNova.models.User;
import org.hotelNova.utils.Helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

public class UserDAO {

    public int create(User u) {
        ensureGuestColumn();
        return Helper.insert(
                "INSERT INTO usuarios (username, password, rol, guest_id, activo) VALUES (?, ?, ?, ?, ?)",
                u.getUsername(),
                u.getPassword(),
                u.getRol(),
                u.getGuestId(),
                u.isActive() ? 1 : 0
        );
    }

    public int create(Connection conn, User u) {
        ensureGuestColumn();
        return Helper.insert(conn,
                "INSERT INTO usuarios (username, password, rol, guest_id, activo) VALUES (?, ?, ?, ?, ?)",
                u.getUsername(),
                u.getPassword(),
                u.getRol(),
                u.getGuestId(),
                u.isActive() ? 1 : 0
        );
    }

    public User findById(int id) {
        ensureGuestColumn();
        return Helper.queryOne(
                "SELECT id, username, password, rol, guest_id AS guestId, activo AS active FROM usuarios WHERE id = ?",
                this::map,
                id
        );
    }

    public User findByUsername(String username) {
        ensureGuestColumn();
        return Helper.queryOne(
                "SELECT id, username, password, rol, guest_id AS guestId, activo AS active FROM usuarios WHERE username = ?",
                this::map,
                username
        );
    }

    public List<User> findAll() {
        ensureGuestColumn();
        return Helper.query(
                "SELECT id, username, password, rol, guest_id AS guestId, activo AS active FROM usuarios WHERE activo = 1",
                this::map
        );
    }

    public void update(User u) {
        ensureGuestColumn();
        Helper.update(
                "UPDATE usuarios SET username=?, password=?, rol=?, guest_id=?, activo=? WHERE id=?",
                u.getUsername(),
                u.getPassword(),
                u.getRol(),
                u.getGuestId(),
                u.isActive() ? 1 : 0,
                u.getId()
        );
    }

    public void ensureGuestColumn() {
        try (Connection conn = AppConfig.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(usuarios)")) {
            boolean hasGuestId = false;
            while (rs.next()) {
                if ("guest_id".equalsIgnoreCase(rs.getString("name"))) {
                    hasGuestId = true;
                    break;
                }
            }

            if (!hasGuestId) {
                stmt.execute("ALTER TABLE usuarios ADD COLUMN guest_id INTEGER");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to ensure guest_id column in usuarios", e);
        }
    }

    public void delete(int id) {
        Helper.update(
                "UPDATE usuarios SET activo = 0 WHERE id = ?",
                id
        );
    }

    private User map(ResultSet rs) {
        try {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setPassword(rs.getString("password"));
            u.setRol(rs.getString("rol"));
            int guestId = rs.getInt("guestId");
            if (!rs.wasNull()) {
                u.setGuestId(guestId);
            }
            u.setActive(rs.getInt("active") == 1);
            return u;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
