// Base Entity Class - src/main/java/com/vehicle/vehicleidentificationsystem/model/BaseEntity.java
package com.vehicle.vehicleidentificationsystem.model;

import java.time.LocalDateTime;

public abstract class BaseEntity {
    protected int id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public abstract int getId();
    public abstract void setId(int id);

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}