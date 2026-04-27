// src/main/java/com/vehicle/vehicleidentificationsystem/model/User.java
package com.vehicle.vehicleidentificationsystem.model;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private boolean isActive;

    public User() {}

    public User(int userId, String username, String role, String fullName, String email, String phone) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.isActive = true;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}