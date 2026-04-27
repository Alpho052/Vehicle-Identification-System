// ServiceDAO.java - src/main/java/com/vehicle/vehicleidentificationsystem/dao/ServiceDAO.java
package com.vehicle.vehicleidentificationsystem.dao;

import com.vehicle.vehicleidentificationsystem.model.ServiceRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {
    private DatabaseConnection db;

    public ServiceDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public List<ServiceRecord> getAllServices() throws SQLException {
        List<ServiceRecord> services = new ArrayList<>();
        String sql = "SELECT * FROM service_history_view";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ServiceRecord s = new ServiceRecord();
                s.setServiceId(rs.getInt("service_id"));
                s.setVehicleId(rs.getInt("vehicle_id"));
                s.setRegistrationNumber(rs.getString("registration_number"));
                s.setVehicleName(rs.getString("vehicle_name"));
                s.setServiceDate(rs.getDate("service_date").toLocalDate());
                s.setServiceType(rs.getString("service_type"));
                s.setDescription(rs.getString("description"));
                s.setCost(rs.getDouble("cost"));
                s.setOdometerReading(rs.getInt("odometer_reading"));
                s.setWorkshopName(rs.getString("workshop_name"));
                services.add(s);
            }
        }
        return services;
    }

    public List<ServiceRecord> getServicesByVehicle(int vehicleId) throws SQLException {
        List<ServiceRecord> services = new ArrayList<>();
        String sql = "SELECT * FROM service_history_view WHERE vehicle_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ServiceRecord s = new ServiceRecord();
                    s.setServiceId(rs.getInt("service_id"));
                    s.setVehicleId(rs.getInt("vehicle_id"));
                    s.setRegistrationNumber(rs.getString("registration_number"));
                    s.setVehicleName(rs.getString("vehicle_name"));
                    s.setServiceDate(rs.getDate("service_date").toLocalDate());
                    s.setServiceType(rs.getString("service_type"));
                    s.setDescription(rs.getString("description"));
                    s.setCost(rs.getDouble("cost"));
                    s.setOdometerReading(rs.getInt("odometer_reading"));
                    s.setWorkshopName(rs.getString("workshop_name"));
                    services.add(s);
                }
            }
        }
        return services;
    }

    public int createService(ServiceRecord service) throws SQLException {
        String sql = "{call add_service_record(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = db.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, service.getVehicleId());
            cstmt.setInt(2, 3); // workshop_id (from logged in user)
            cstmt.setDate(3, Date.valueOf(service.getServiceDate()));
            cstmt.setString(4, service.getServiceType());
            cstmt.setString(5, service.getDescription());
            cstmt.setDouble(6, service.getCost());
            cstmt.setInt(7, service.getOdometerReading());
            cstmt.execute();
            return 1;
        }
    }

    public boolean updateService(ServiceRecord service) throws SQLException {
        String sql = "UPDATE service_record SET service_date = ?, service_type = ?, description = ?, cost = ?, odometer_reading = ? WHERE service_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(service.getServiceDate()));
            pstmt.setString(2, service.getServiceType());
            pstmt.setString(3, service.getDescription());
            pstmt.setDouble(4, service.getCost());
            pstmt.setInt(5, service.getOdometerReading());
            pstmt.setInt(6, service.getServiceId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteService(int serviceId) throws SQLException {
        String sql = "DELETE FROM service_record WHERE service_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, serviceId);
            return pstmt.executeUpdate() > 0;
        }
    }
}