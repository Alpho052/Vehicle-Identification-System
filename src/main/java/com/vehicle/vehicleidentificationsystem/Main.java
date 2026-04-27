// src/main/java/com/vehicle/vehicleidentificationsystem/Main.java
package com.vehicle.vehicleidentificationsystem;

import com.vehicle.vehicleidentificationsystem.dao.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Test database connection on startup
        System.out.println("Testing database connection...");
        boolean connected = DatabaseConnection.getInstance().testConnection();
        if (connected) {
            System.out.println("✓ Database connection successful!");
        } else {
            System.err.println("✗ Database connection failed! Please check your credentials.");
        }

        // Load the FXML file from the correct path
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Vehicle Identification System");
        primaryStage.setScene(new Scene(root, 900, 700));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}