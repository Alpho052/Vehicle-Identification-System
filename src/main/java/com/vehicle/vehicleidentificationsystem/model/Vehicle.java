package com.vehicle.vehicleidentificationsystem.model;

public class Vehicle {
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private int year;
    private int ownerId;
    private String color;
    private String engineNumber;
    private String chassisNumber;
    private String registrationDate;
    private boolean isStolen;
    private String ownerName;

    public Vehicle() {}

    public Vehicle(int vehicleId, String registrationNumber, String make, String model, int year) {
        this.vehicleId = vehicleId;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
    }

    @Override
    public String toString() {
        return registrationNumber + " - " + make + " " + model + " (" + year + ")";
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getEngineNumber() { return engineNumber; }
    public void setEngineNumber(String engineNumber) { this.engineNumber = engineNumber; }

    public String getChassisNumber() { return chassisNumber; }
    public void setChassisNumber(String chassisNumber) { this.chassisNumber = chassisNumber; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public boolean isStolen() { return isStolen; }
    public void setStolen(boolean stolen) { isStolen = stolen; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}