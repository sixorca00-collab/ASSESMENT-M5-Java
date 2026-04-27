package org.hotelNova.services;

import org.hotelNova.DAO.LogDAO;
import org.hotelNova.models.Log;

public class LogService {

    private final LogDAO logDAO = new LogDAO();

    public void log(String method, String endpoint, String message) {
        Log log = new Log();
        log.setMethod(method);
        log.setEndpoint(endpoint);
        log.setMessage(message);
        log.setDate(java.time.LocalDateTime.now().toString());

        logDAO.create(log);
    }
}