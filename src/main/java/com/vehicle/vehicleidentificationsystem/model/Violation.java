package com.vehicle.vehicleidentificationsystem.model;

import java.time.LocalDate;

public class Violation extends BaseEntity {
    private int violationId;
    private int vehicleId;
    private String registrationNumber;
    private LocalDate violationDate;
    private String violationType;
    private double fineAmount;
    private int points;
    private boolean isPaid;
    private LocalDate paymentDate;

    @Override
    public int getId() { return violationId; }
    @Override
    public void setId(int id) { this.violationId = id; }

    public int getViolationId() { return violationId; }
    public void setViolationId(int violationId) { this.violationId = violationId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public LocalDate getViolationDate() { return violationDate; }
    public void setViolationDate(LocalDate violationDate) { this.violationDate = violationDate; }

    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
}
