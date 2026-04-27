package com.vehicle.vehicleidentificationsystem.dao;

import com.vehicle.vehicleidentificationsystem.model.CustomerQuery;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryDAO {
    private DatabaseConnection db;

    public QueryDAO() {
        this.db = DatabaseConnection.getInstance();
    }

    public List<CustomerQuery> getAllQueries() throws SQLException {
        List<CustomerQuery> queries = new ArrayList<>();
        String sql = "SELECT * FROM queries_view";

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                CustomerQuery q = new CustomerQuery();
                q.setQueryId(rs.getInt("query_id"));
                q.setCustomerId(rs.getInt("customer_id"));
                q.setCustomerName(rs.getString("customer_name"));
                q.setVehicleId(rs.getInt("vehicle_id"));
                q.setRegistrationNumber(rs.getString("registration_number"));
                q.setQueryDate(rs.getTimestamp("query_date").toLocalDateTime());
                q.setQueryText(rs.getString("query_text"));
                q.setResponseText(rs.getString("response_text"));
                q.setStatus(rs.getString("status"));
                if (rs.getTimestamp("response_date") != null) {
                    q.setResponseDate(rs.getTimestamp("response_date").toLocalDateTime());
                }
                queries.add(q);
            }
        }
        return queries;
    }

    public List<CustomerQuery> getQueriesByCustomer(int customerId) throws SQLException {
        List<CustomerQuery> queries = new ArrayList<>();
        String sql = "SELECT * FROM queries_view WHERE customer_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CustomerQuery q = new CustomerQuery();
                    q.setQueryId(rs.getInt("query_id"));
                    q.setCustomerId(rs.getInt("customer_id"));
                    q.setCustomerName(rs.getString("customer_name"));
                    q.setVehicleId(rs.getInt("vehicle_id"));
                    q.setRegistrationNumber(rs.getString("registration_number"));
                    q.setQueryDate(rs.getTimestamp("query_date").toLocalDateTime());
                    q.setQueryText(rs.getString("query_text"));
                    q.setResponseText(rs.getString("response_text"));
                    q.setStatus(rs.getString("status"));
                    queries.add(q);
                }
            }
        }
        return queries;
    }

    public int createQuery(CustomerQuery query) throws SQLException {
        String sql = "{call add_customer_query(?, ?, ?)}";
        try (Connection conn = db.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, query.getCustomerId());
            cstmt.setInt(2, query.getVehicleId());
            cstmt.setString(3, query.getQueryText());
            cstmt.execute();
            return 1;
        }
    }

    public boolean respondToQuery(int queryId, String response) throws SQLException {
        String sql = "{call respond_to_query(?, ?)}";
        try (Connection conn = db.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, queryId);
            cstmt.setString(2, response);
            cstmt.execute();
            return true;
        }
    }

    public boolean updateQuery(CustomerQuery query) throws SQLException {
        String sql = "UPDATE customer_query SET query_text = ?, is_edited = true WHERE query_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, query.getQueryText());
            pstmt.setInt(2, query.getQueryId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteQuery(int queryId) throws SQLException {
        String sql = "DELETE FROM customer_query WHERE query_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, queryId);
            return pstmt.executeUpdate() > 0;
        }
    }
}
