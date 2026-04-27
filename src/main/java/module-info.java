module com.vehicle.vehicleidentificationsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens com.vehicle.vehicleidentificationsystem to javafx.fxml;
    opens com.vehicle.vehicleidentificationsystem.controller to javafx.fxml;
    opens com.vehicle.vehicleidentificationsystem.model to javafx.base;

    exports com.vehicle.vehicleidentificationsystem;
    exports com.vehicle.vehicleidentificationsystem.controller;
    exports com.vehicle.vehicleidentificationsystem.model;
}