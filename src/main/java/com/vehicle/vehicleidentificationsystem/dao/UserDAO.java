package com.vehicle.vehicleidentificationsystem.dao;

import com.vehicle.vehicleidentificationsystem.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private DatabaseConnection db;

    public UserDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setActive(rs.getBoolean("is_active"));
                users.add(u);
            }
        }
        return users;
    }

    public int createUser(User user, String address) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, full_name, email, phone, is_active, created_at, updated_at) VALUES (?, ?, ?::varchar, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhone());
            pstmt.setBoolean(7, true);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);

                        if ("CUSTOMER".equals(user.getRole()) && address != null && !address.isEmpty()) {
                            String customerSql = "INSERT INTO customer (customer_id, address) VALUES (?, ?)";
                            try (PreparedStatement customerStmt = conn.prepareStatement(customerSql)) {
                                customerStmt.setInt(1, userId);
                                customerStmt.setString(2, address);
                                customerStmt.executeUpdate();
                            }
                        }
                        return userId;
                    }
                }
            }
            return 0;
        }
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, role = ?::varchar, full_name = ?, email = ?, phone = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRole());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setBoolean(6, user.isActive());
            pstmt.setInt(7, user.getUserId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateUserRole(int userId, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ?::varchar, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean activateUser(int userId, boolean isActive) throws SQLException {
        String sql = "UPDATE users SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isActive);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int userId) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String deleteCustomer = "DELETE FROM customer WHERE customer_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteCustomer)) {
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();
                }

                String sql = "DELETE FROM users WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, userId);
                    int result = pstmt.executeUpdate();
                    conn.commit();
                    return result > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setPassword(rs.getString("password"));
                    u.setRole(rs.getString("role"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setActive(rs.getBoolean("is_active"));
                    return u;
                }
            }
        }
        return null;
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setPassword(rs.getString("password"));
                    u.setRole(rs.getString("role"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setPhone(rs.getString("phone"));
                    u.setActive(rs.getBoolean("is_active"));
                    return u;
                }
            }
        }
        return null;
    }
}
