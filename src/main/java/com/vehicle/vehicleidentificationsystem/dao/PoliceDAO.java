// PoliceDAO.java - src/main/java/com/vehicle/vehicleidentificationsystem/dao/PoliceDAO.java
package com.vehicle.vehicleidentificationsystem.dao;

import com.vehicle.vehicleidentificationsystem.model.PoliceReport;
import com.vehicle.vehicleidentificationsystem.model.Violation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoliceDAO {
    private DatabaseConnection db;

    public PoliceDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public List<PoliceReport> getAllReports() throws SQLException {
        List<PoliceReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM police_reports_view";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PoliceReport r = new PoliceReport();
                r.setReportId(rs.getInt("report_id"));
                r.setVehicleId(rs.getInt("vehicle_id"));
                r.setRegistrationNumber(rs.getString("registration_number"));
                r.setVehicleName(rs.getString("vehicle_name"));
                r.setReportDate(rs.getTimestamp("report_date").toLocalDateTime());
                r.setReportType(rs.getString("report_type"));
                r.setDescription(rs.getString("description"));
                r.setLocation(rs.getString("location"));
                r.setCaseNumber(rs.getString("case_number"));
                r.setStatus(rs.getString("status"));
                r.setOfficerName(rs.getString("officer_name"));
                reports.add(r);
            }
        }
        return reports;
    }

    public List<Violation> getAllViolations() throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT * FROM violations_view";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Violation v = new Violation();
                v.setViolationId(rs.getInt("violation_id"));
                v.setVehicleId(rs.getInt("vehicle_id"));
                v.setRegistrationNumber(rs.getString("registration_number"));
                v.setViolationDate(rs.getDate("violation_date").toLocalDate());
                v.setViolationType(rs.getString("violation_type"));
                v.setFineAmount(rs.getDouble("fine_amount"));
                v.setPoints(rs.getInt("points"));
                v.setPaid(rs.getBoolean("is_paid"));
                if (rs.getDate("payment_date") != null) {
                    v.setPaymentDate(rs.getDate("payment_date").toLocalDate());
                }
                violations.add(v);
            }
        }
        return violations;
    }

    public int createReport(PoliceReport report) throws SQLException {
        String sql = "{call add_police_report(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = db.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, report.getVehicleId());
            cstmt.setInt(2, 2); // officer_id from logged in user
            cstmt.setString(3, report.getReportType());
            cstmt.setString(4, report.getDescription());
            cstmt.setString(5, report.getLocation());
            cstmt.setString(6, report.getCaseNumber());
            cstmt.execute();
            return 1;
        }
    }

    public int createViolation(Violation violation) throws SQLException {
        String sql = "{call add_violation(?, ?, ?, ?, ?)}";
        try (Connection conn = db.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, violation.getVehicleId());
            cstmt.setDate(2, Date.valueOf(violation.getViolationDate()));
            cstmt.setString(3, violation.getViolationType());
            cstmt.setDouble(4, violation.getFineAmount());
            cstmt.setInt(5, violation.getPoints());
            cstmt.execute();
            return 1;
        }
    }

    public boolean updateViolationPayment(int violationId, boolean isPaid) throws SQLException {
        String sql = "UPDATE violation SET is_paid = ?, payment_date = ? WHERE violation_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isPaid);
            pstmt.setDate(2, isPaid ? Date.valueOf(java.time.LocalDate.now()) : null);
            pstmt.setInt(3, violationId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteReport(int reportId) throws SQLException {
        String sql = "DELETE FROM police_report WHERE report_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reportId);
            return pstmt.executeUpdate() > 0;
        }
    }
}