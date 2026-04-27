package org.hotelNova.DAO;

import org.hotelNova.models.User;
import org.hotelNova.utils.Helper;

import java.sql.ResultSet;
import java.util.List;

public class UserDAO {

    public void create(User u) {
        Helper.update(
                "INSERT INTO usuarios (username, password, rol, activo) VALUES (?, ?, ?, ?)",
                u.getUsername(),
                u.getPassword(),
                u.getRol(),
                u.isActive() ? 1 : 0
        );
    }

    public User findById(int id) {
        return Helper.queryOne(
                "SELECT id, username, password, rol, activo AS active FROM usuarios WHERE id = ?",
                this::map,
                id
        );
    }

    public User findByUsername(String username) {
        return Helper.queryOne(
                "SELECT id, username, password, rol, activo AS active FROM usuarios WHERE username = ?",
                this::map,
                username
        );
    }

    public List<User> findAll() {
        return Helper.query(
                "SELECT id, username, password, rol, activo AS active FROM usuarios WHERE activo = 1",
                this::map
        );
    }

    public void update(User u) {
        Helper.update(
                "UPDATE usuarios SET username=?, password=?, rol=?, activo=? WHERE id=?",
                u.getUsername(),
                u.getPassword(),
                u.getRol(),
                u.isActive() ? 1 : 0,
                u.getId()
        );
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
            u.setActive(rs.getInt("active") == 1);
            return u;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}