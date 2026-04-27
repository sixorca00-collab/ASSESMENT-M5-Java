package org.hotelNova.services;

import org.hotelNova.DAO.UserDAO;
import org.hotelNova.models.User;

import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public void create(User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new RuntimeException("Invalid credentials");
        }
        
        if (user.getRol() == null || user.getRol().trim().isEmpty()) {
            throw new RuntimeException("Role is required");
        }
        
        // Validar roles permitidos
        String[] validRoles = {"ADMIN", "RECEPTIONIST", "MANAGER"};
        boolean roleValid = false;
        for (String validRole : validRoles) {
            if (validRole.equals(user.getRol())) {
                roleValid = true;
                break;
            }
        }
        
        if (!roleValid) {
            throw new RuntimeException("Invalid role. Valid roles: RECEPTIONIST, ADMIN, MANAGER");
        }
        
        // Verificar si el usuario ya existe
        List<User> existingUsers = userDAO.findAll();
        for (User existingUser : existingUsers) {
            if (existingUser.getUsername().equals(user.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
        }

        userDAO.create(user);
    }

    public User login(String username, String password) {
        List<User> users = userDAO.findAll();

        for (User u : users) {
            if (u.getUsername().equals(username)
                    && u.getPassword().equals(password)
                    && u.isActive()) {
                return u;
            }
        }

        throw new RuntimeException("Invalid login");
    }
}