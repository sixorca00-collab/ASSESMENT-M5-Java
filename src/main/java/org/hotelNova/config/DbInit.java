package org.hotelNova.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.hotelnova.config.AppConfig;

public class DbInit {

    public static void init() {

        try (Connection conn = AppConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL ,
                    password TEXT NOT NULL,
                    rol TEXT DEFAULT 'RECEPCIONISTA',
                    guest_id INTEGER,
                    FOREIGN KEY (guest_id) REFERENCES huespedes(id),
                    activo INTEGER DEFAULT 1
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS huespedes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    documento TEXT NOT NULL UNIQUE,
                    telefono TEXT,
                    email TEXT,
                    activo INTEGER DEFAULT 1
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS habitaciones (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero INTEGER NOT NULL UNIQUE,
                    tipo TEXT NOT NULL,
                    precio_noche REAL NOT NULL,
                    estado TEXT DEFAULT 'DISPONIBLE',
                    activo INTEGER DEFAULT 1
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    huesped_id INTEGER NOT NULL,
                    habitacion_id INTEGER NOT NULL,
                    fecha_inicio TEXT NOT NULL,
                    fecha_fin TEXT NOT NULL,
                    estado TEXT DEFAULT 'RESERVADA',
                    FOREIGN KEY (huesped_id) REFERENCES huespedes(id),
                    FOREIGN KEY (habitacion_id) REFERENCES habitaciones(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    metodo TEXT,
                    endpoint TEXT,
                    mensaje TEXT,
                    fecha DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            ensureGuestLinkColumn(conn, stmt);

            System.out.println("DB i'ts ready");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ensureGuestLinkColumn(Connection conn, Statement stmt) throws Exception {
        boolean hasGuestId = false;

        try (ResultSet rs = conn.createStatement().executeQuery("PRAGMA table_info(usuarios)")) {
            while (rs.next()) {
                if ("guest_id".equalsIgnoreCase(rs.getString("name"))) {
                    hasGuestId = true;
                    break;
                }
            }
        }

        if (!hasGuestId) {
            stmt.execute("ALTER TABLE usuarios ADD COLUMN guest_id INTEGER REFERENCES huespedes(id)");
        }
    }
}
