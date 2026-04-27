package com.vehicle.vehicleidentificationsystem.dao;

import com.vehicle.vehicleidentificationsystem.model.InsuranceRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsuranceDAO {
    private DatabaseConnection db;

    public InsuranceDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public List<InsuranceRecord> getAllInsurance() throws SQLException {
        List<InsuranceRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM insurance_view";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                InsuranceRecord i = new InsuranceRecord();
                i.setInsuranceId(rs.getInt("insurance_id"));
                i.setVehicleId(rs.getInt("vehicle_id"));
                i.setRegistrationNumber(rs.getString("registration_number"));
                i.setVehicleName(rs.getString("vehicle_name"));
                i.setInsuranceCompany(rs.getString("insurance_company"));
                i.setPolicyNumber(rs.getString("policy_number"));
                i.setStartDate(rs.getDate("start_date").toLocalDate());
                i.setEndDate(rs.getDate("end_date").toLocalDate());
                i.setCoverageAmount(rs.getDouble("coverage_amount"));
                i.setPremiumAmount(rs.getDouble("premium_amount"));
                i.setStatus(rs.getString("status"));
                i.setCurrentStatus(rs.getString("current_status"));
                records.add(i);
            }
        }
        return records;
    }

    public List<InsuranceRecord> getInsuranceByVehicle(int vehicleId) throws SQLException {
        List<InsuranceRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM insurance_view WHERE vehicle_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    InsuranceRecord i = new InsuranceRecord();
                    i.setInsuranceId(rs.getInt("insurance_id"));
                    i.setVehicleId(rs.getInt("vehicle_id"));
                    i.setRegistrationNumber(rs.getString("registration_number"));
                    i.setVehicleName(rs.getString("vehicle_name"));
                    i.setInsuranceCompany(rs.getString("insurance_company"));
                    i.setPolicyNumber(rs.getString("policy_number"));
                    i.setStartDate(rs.getDate("start_date").toLocalDate());
                    i.setEndDate(rs.getDate("end_date").toLocalDate());
                    i.setCoverageAmount(rs.getDouble("coverage_amount"));
                    i.setPremiumAmount(rs.getDouble("premium_amount"));
                    i.setStatus(rs.getString("status"));
                    i.setCurrentStatus(rs.getString("current_status"));
                    records.add(i);
                }
            }
        }
        return records;
    }

    public int createInsurance(InsuranceRecord insurance) throws SQLException {
        String sql = "{call add_insurance_record(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = db.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, insurance.getVehicleId());
            cstmt.setString(2, insurance.getInsuranceCompany());
            cstmt.setString(3, insurance.getPolicyNumber());
            cstmt.setDate(4, Date.valueOf(insurance.getStartDate()));
            cstmt.setDate(5, Date.valueOf(insurance.getEndDate()));
            cstmt.setDouble(6, insurance.getCoverageAmount());
            cstmt.setDouble(7, insurance.getPremiumAmount());
            cstmt.execute();
            return 1;
        }
    }

    public boolean updateInsurance(InsuranceRecord insurance) throws SQLException {
        String sql = "UPDATE insurance_record SET insurance_company = ?, policy_number = ?, start_date = ?, end_date = ?, coverage_amount = ?, premium_amount = ?, status = ? WHERE insurance_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, insurance.getInsuranceCompany());
            pstmt.setString(2, insurance.getPolicyNumber());
            pstmt.setDate(3, Date.valueOf(insurance.getStartDate()));
            pstmt.setDate(4, Date.valueOf(insurance.getEndDate()));
            pstmt.setDouble(5, insurance.getCoverageAmount());
            pstmt.setDouble(6, insurance.getPremiumAmount());
            pstmt.setString(7, insurance.getStatus());
            pstmt.setInt(8, insurance.getInsuranceId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteInsurance(int insuranceId) throws SQLException {
        String sql = "DELETE FROM insurance_record WHERE insurance_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, insuranceId);
            return pstmt.executeUpdate() > 0;
        }
    }
}
