// PoliceReport.java - src/main/java/com/vehicle/vehicleidentificationsystem/model/PoliceReport.java
package com.vehicle.vehicleidentificationsystem.model;

import java.time.LocalDateTime;

public class PoliceReport extends BaseEntity {
    private int reportId;
    private int vehicleId;
    private String registrationNumber;
    private String vehicleName;
    private LocalDateTime reportDate;
    private String reportType;
    private String description;
    private String location;
    private String caseNumber;
    private String status;
    private String officerName;

    @Override
    public int getId() { return reportId; }
    @Override
    public void setId(int id) { this.reportId = id; }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) { this.officerName = officerName; }
}