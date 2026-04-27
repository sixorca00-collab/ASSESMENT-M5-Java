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