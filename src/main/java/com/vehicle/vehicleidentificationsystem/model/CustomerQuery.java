// CustomerQuery.java - src/main/java/com/vehicle/vehicleidentificationsystem/model/CustomerQuery.java
package com.vehicle.vehicleidentificationsystem.model;

import java.time.LocalDateTime;

public class CustomerQuery extends BaseEntity {
    private int queryId;
    private int customerId;
    private String customerName;
    private int vehicleId;
    private String registrationNumber;
    private LocalDateTime queryDate;
    private String queryText;
    private String responseText;
    private String status;
    private LocalDateTime responseDate;

    @Override
    public int getId() { return queryId; }
    @Override
    public void setId(int id) { this.queryId = id; }

    public int getQueryId() { return queryId; }
    public void setQueryId(int queryId) { this.queryId = queryId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public LocalDateTime getQueryDate() { return queryDate; }
    public void setQueryDate(LocalDateTime queryDate) { this.queryDate = queryDate; }

    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getResponseDate() { return responseDate; }
    public void setResponseDate(LocalDateTime responseDate) { this.responseDate = responseDate; }
}