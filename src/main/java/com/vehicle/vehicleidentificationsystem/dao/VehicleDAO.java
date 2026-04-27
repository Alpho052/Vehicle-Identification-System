package com.vehicle.vehicleidentificationsystem.dao;

import com.vehicle.vehicleidentificationsystem.model.Vehicle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {
    private DatabaseConnection db;

    public VehicleDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public List<Vehicle> getAllVehicles() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicle_details_view ORDER BY vehicle_id";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vehicle v = new Vehicle();
                v.setVehicleId(rs.getInt("vehicle_id"));
                v.setRegistrationNumber(rs.getString("registration_number"));
                v.setMake(rs.getString("make"));
                v.setModel(rs.getString("model"));
                v.setYear(rs.getInt("year"));
                v.setColor(rs.getString("color"));
                v.setOwnerId(rs.getInt("customer_id"));
                v.setOwnerName(rs.getString("owner_name"));
                v.setStolen(rs.getBoolean("is_stolen"));
                vehicles.add(v);
            }
        }
        return vehicles;
    }

    public Vehicle getVehicleById(int id) throws SQLException {
        String sql = "SELECT * FROM vehicle_details_view WHERE vehicle_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Vehicle v = new Vehicle();
                    v.setVehicleId(rs.getInt("vehicle_id"));
                    v.setRegistrationNumber(rs.getString("registration_number"));
                    v.setMake(rs.getString("make"));
                    v.setModel(rs.getString("model"));
                    v.setYear(rs.getInt("year"));
                    v.setColor(rs.getString("color"));
                    v.setOwnerId(rs.getInt("customer_id"));
                    v.setOwnerName(rs.getString("owner_name"));
                    v.setStolen(rs.getBoolean("is_stolen"));
                    return v;
                }
            }
        }
        return null;
    }
}
