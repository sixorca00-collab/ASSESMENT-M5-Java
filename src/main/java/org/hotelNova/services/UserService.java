package org.hotelNova.services;

import org.hotelnova.config.AppConfig;
import org.hotelNova.DAO.GuestDAO;
import org.hotelNova.DAO.UserDAO;
import org.hotelNova.models.Guest;
import org.hotelNova.models.User;
import org.hotelNova.utils.PasswordHasher;

import java.sql.Connection;
import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final GuestDAO guestDAO = new GuestDAO();

    public void create(User user) {
        validateUser(user);
        ensureUsernameAvailable(user.getUsername(), null);
        user.setRol(normalizeRole(user.getRol()));
        user.setPassword(hashIfNeeded(user.getPassword()));
        userDAO.create(user);
    }

    public void createGuestAccount(User user, Guest guest) {
        user.setRol("GUEST");
        validateUser(user);
        validateGuestProfile(guest);
        ensureUsernameAvailable(user.getUsername(), null);

        if (guestDAO.findByDocument(guest.getDocument()) != null) {
            throw new RuntimeException("A guest profile with that document already exists");
        }

        Connection conn = null;

        try {
            conn = AppConfig.getInstance().getConnection();
            conn.setAutoCommit(false);

            guest.setActive(true);
            int guestId = guestDAO.create(conn, guest);
            user.setGuestId(guestId);
            user.setPassword(hashIfNeeded(user.getPassword()));
            userDAO.create(conn, user);

            conn.commit();
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ignored) {
            }
            throw new RuntimeException("Unable to create guest account", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void update(User user) {
        validateUser(user);
        ensureUsernameAvailable(user.getUsername(), user.getId());
        user.setRol(normalizeRole(user.getRol()));
        user.setPassword(hashIfNeeded(user.getPassword()));
        userDAO.update(user);
    }

    public boolean usernameExists(String username) {
        return userDAO.findByUsername(username) != null;
    }

    public User login(String username, String password) {
        List<User> users = userDAO.findAll();

        for (User u : users) {
            if (!u.isActive() || !u.getUsername().equals(username)) {
                continue;
            }

            if (PasswordHasher.matches(password, u.getPassword())) {
                if (!PasswordHasher.isHashed(u.getPassword())) {
                    u.setPassword(PasswordHasher.hash(password));
                    userDAO.update(u);
                }
                return userDAO.findById(u.getId());
            }
        }

        throw new RuntimeException("Invalid login");
    }

    private void validateUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        String normalizedRole = normalizeRole(user.getRol());
        if (normalizedRole == null || normalizedRole.isEmpty()) {
            throw new RuntimeException("Role is required");
        }

        String[] validRoles = {"ADMIN", "RECEPTIONIST", "MANAGER", "GUEST"};
        for (String validRole : validRoles) {
            if (validRole.equals(normalizedRole)) {
                return;
            }
        }

        throw new RuntimeException("Invalid role. Valid roles: RECEPTIONIST, GUEST, ADMIN, MANAGER");
    }

    private void validateGuestProfile(Guest guest) {
        if (guest == null) {
            throw new RuntimeException("Guest profile is required");
        }
        if (isBlank(guest.getName())) {
            throw new RuntimeException("Guest name is required");
        }
        if (isBlank(guest.getDocument())) {
            throw new RuntimeException("Guest document is required");
        }
        if (isBlank(guest.getPhone())) {
            throw new RuntimeException("Guest phone is required");
        }
        if (isBlank(guest.getEmail())) {
            throw new RuntimeException("Guest email is required");
        }
    }

    private void ensureUsernameAvailable(String username, Integer currentUserId) {
        User existing = userDAO.findByUsername(username);
        if (existing != null && (currentUserId == null || existing.getId() != currentUserId)) {
            throw new RuntimeException("Username already exists");
        }
    }

    private String normalizeRole(String role) {
        return role == null ? null : role.trim().toUpperCase();
    }

    private String hashIfNeeded(String password) {
        return PasswordHasher.isHashed(password) ? password : PasswordHasher.hash(password);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
