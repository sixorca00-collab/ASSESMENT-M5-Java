package org.hotelNova.DAO;

import org.hotelNova.models.Log;
import org.hotelNova.utils.Helper;

import java.sql.ResultSet;
import java.util.List;

public class LogDAO {

    public void create(Log l) {
        Helper.update(
                "INSERT INTO logs (metodo, endpoint, mensaje) VALUES (?, ?, ?)",
                l.getMethod(),
                l.getEndpoint(),
                l.getMessage()
        );
    }

    public List<Log> findAll() {
        return Helper.query(
                "SELECT id, metodo AS method, endpoint, mensaje AS message, fecha AS date FROM logs",
                this::map
        );
    }

    private Log map(ResultSet rs) {
        try {
            Log l = new Log();
            l.setId(rs.getInt("id"));
            l.setMethod(rs.getString("method"));
            l.setEndpoint(rs.getString("endpoint"));
            l.setMessage(rs.getString("message"));
            l.setDate(rs.getString("date"));
            return l;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}