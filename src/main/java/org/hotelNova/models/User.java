package org.hotelNova.models;

public class User {
    int id;
    String username;
    String password;
    String rol;
    Integer guestId;
    boolean active;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActive() {
        return active;
    }

    public Integer getGuestId() {
        return guestId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setGuestId(Integer guestId) {
        this.guestId = guestId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
