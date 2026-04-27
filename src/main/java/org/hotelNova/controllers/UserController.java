package org.hotelNova.controllers;

import org.hotelNova.models.User;
import org.hotelNova.services.UserService;

public class UserController {

    private final UserService userService = new UserService();

    public void login(String username, String password) {
        try {
            User user = userService.login(username, password);

            System.out.println("200 OK - Welcome " + user.getUsername());

        } catch (Exception e) {
            System.out.println("401 UNAUTHORIZED");
        }
    }
}