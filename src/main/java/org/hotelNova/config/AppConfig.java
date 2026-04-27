package org.hotelnova.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class AppConfig {

    private static AppConfig instance;
    private String url;

    public AppConfig() {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            Properties props = new Properties();
            props.load(input);

            Class.forName(props.getProperty("db.driver"));
            url = System.getProperty("hotelNova.db.url", props.getProperty("db.url"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
