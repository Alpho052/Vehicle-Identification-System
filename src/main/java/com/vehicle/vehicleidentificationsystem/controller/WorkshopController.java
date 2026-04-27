package com.vehicle.vehicleidentificationsystem.controller;

import com.vehicle.vehicleidentificationsystem.dao.ServiceDAO;
import com.vehicle.vehicleidentificationsystem.dao.VehicleDAO;
import com.vehicle.vehicleidentificationsystem.dao.DatabaseConnection;
import com.vehicle.vehicleidentificationsystem.model.ServiceRecord;
import com.vehicle.vehicleidentificationsystem.model.User;
import com.vehicle.vehicleidentificationsystem.model.Vehicle;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class WorkshopController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<ServiceRecord> servicesTable;
    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private TextField serviceTypeField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField costField;
    @FXML private TextField odometerField;
    @FXML private DatePicker serviceDatePicker;
    @FXML private Label statusLabel;

    private ServiceDAO serviceDAO;
    private VehicleDAO vehicleDAO;
    private ObservableList<ServiceRecord> serviceList;
    private ObservableList<Vehicle> vehicleList;
    private User currentUser;

    private static final Pattern TEXT_PATTERN = Pattern.compile("^[A-Za-z\\s]+$");
    private static final Pattern COST_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    private static final Pattern ODOMETER_PATTERN = Pattern.compile("^\\d+$");

    @FXML
    public void initialize() {
        serviceDAO = new ServiceDAO();
        vehicleDAO = new VehicleDAO();
        serviceList = FXCollections.observableArrayList();
        vehicleList = FXCollections.observableArrayList();

        setupTable();
        loadVehicles();
        loadServices();
        serviceDatePicker.setValue(LocalDate.now());
        setupAnimations();
        setupLiveValidation();
    }

    private void setupAnimations() {
        FadeTransition fadeTable = new FadeTransition(Duration.seconds(0.5), servicesTable);
        fadeTable.setFromValue(0);
        fadeTable.setToValue(1);
        fadeTable.play();
    }

    private void setupLiveValidation() {
        serviceTypeField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !TEXT_PATTERN.matcher(newVal).matches()) {
                serviceTypeField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                serviceTypeField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                serviceTypeField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        costField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !COST_PATTERN.matcher(newVal).matches()) {
                costField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                statusLabel.setText("Cost must be a valid number (e.g., 150.00)");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (!newVal.isEmpty()) {
                costField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                statusLabel.setText("");
            } else {
                costField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        odometerField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !ODOMETER_PATTERN.matcher(newVal).matches()) {
                odometerField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                statusLabel.setText("Odometer must be a valid number");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (!newVal.isEmpty()) {
                odometerField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                statusLabel.setText("");
            } else {
                odometerField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });
    }

    private void setupTable() {
        servicesTable.getColumns().clear();

        TableColumn<ServiceRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ServiceRecord, String> regCol = new TableColumn<>("Vehicle");
        regCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        regCol.setPrefWidth(140);

        TableColumn<ServiceRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        dateCol.setPrefWidth(100);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ServiceRecord, String> typeCol = new TableColumn<>("Service Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        typeCol.setPrefWidth(150);

        TableColumn<ServiceRecord, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(300);

        TableColumn<ServiceRecord, Double> costCol = new TableColumn<>("Cost (M)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        costCol.setPrefWidth(100);
        costCol.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<ServiceRecord, Integer> odoCol = new TableColumn<>("Odometer");
        odoCol.setCellValueFactory(new PropertyValueFactory<>("odometerReading"));
        odoCol.setPrefWidth(100);
        odoCol.setStyle("-fx-alignment: CENTER;");

        servicesTable.getColumns().addAll(idCol, regCol, dateCol, typeCol, descCol, costCol, odoCol);
        servicesTable.setItems(serviceList);
        servicesTable.setRowFactory(tv -> {
            TableRow<ServiceRecord> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #e8f5e9;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });

        servicesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEditService();
            }
        });
    }

    private void loadVehicles() {
        try {
            List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
            vehicleList.clear();
            vehicleList.addAll(vehicles);
            vehicleCombo.setItems(vehicleList);
            vehicleCombo.setPromptText("Select a vehicle");
        } catch (SQLException e) {
            showAlert("Error", "Failed to load vehicles: " + e.getMessage());
        }
    }

    private void loadServices() {
        try {
            List<ServiceRecord> services = serviceDAO.getAllServices();
            serviceList.clear();
            serviceList.addAll(services);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load services: " + e.getMessage());
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText(user.getFullName());
    }

    @FXML
    private void handleAddService() {
        statusLabel.setText("");

        Vehicle selectedVehicle = vehicleCombo.getValue();
        String serviceType = serviceTypeField.getText().trim();
        String description = descriptionArea.getText().trim();
        String costText = costField.getText().trim();
        String odometerText = odometerField.getText().trim();
        LocalDate serviceDate = serviceDatePicker.getValue();

        if (selectedVehicle == null) {
            statusLabel.setText("Please select a vehicle");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (serviceType.isEmpty()) {
            statusLabel.setText("Please enter service type");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!TEXT_PATTERN.matcher(serviceType).matches()) {
            statusLabel.setText("Service type must contain only letters");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (costText.isEmpty()) {
            statusLabel.setText("Please enter cost");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!COST_PATTERN.matcher(costText).matches()) {
            statusLabel.setText("Please enter a valid cost (e.g., 150.00)");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!odometerText.isEmpty() && !ODOMETER_PATTERN.matcher(odometerText).matches()) {
            statusLabel.setText("Please enter a valid odometer reading");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            double cost = Double.parseDouble(costText);
            int odometer = odometerText.isEmpty() ? 0 : Integer.parseInt(odometerText);

            String sql = "INSERT INTO service_record (vehicle_id, workshop_id, service_date, service_type, description, cost, odometer_reading) VALUES (?, ?, ?, ?, ?, ?, ?)";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedVehicle.getVehicleId());
            pstmt.setInt(2, currentUser.getUserId());
            pstmt.setDate(3, java.sql.Date.valueOf(serviceDate));
            pstmt.setString(4, serviceType);
            pstmt.setString(5, description);
            pstmt.setDouble(6, cost);
            pstmt.setInt(7, odometer);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            serviceTypeField.clear();
            descriptionArea.clear();
            costField.clear();
            odometerField.clear();
            serviceDatePicker.setValue(LocalDate.now());
            vehicleCombo.setValue(null);

            loadServices();
            statusLabel.setText("Service record added successfully!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setText("")));
            timeline.play();

        } catch (SQLException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handleViewVehicleHistory() {
        Vehicle selected = vehicleCombo.getValue();
        if (selected == null) {
            statusLabel.setText("Please select a vehicle to view history");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            String sql = "SELECT * FROM service_record WHERE vehicle_id = ? ORDER BY service_date DESC";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selected.getVehicleId());
            ResultSet rs = pstmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("SERVICE HISTORY FOR: ").append(selected.getRegistrationNumber()).append("\n");
            sb.append("Vehicle: ").append(selected.getMake()).append(" ").append(selected.getModel()).append("\n\n");

            boolean hasRecords = false;
            int count = 0;
            while (rs.next()) {
                hasRecords = true;
                count++;
                sb.append(count).append(". Date: ").append(rs.getDate("service_date")).append("\n");
                sb.append("   Type: ").append(rs.getString("service_type")).append("\n");
                sb.append("   Description: ").append(rs.getString("description")).append("\n");
                sb.append("   Cost: M").append(rs.getDouble("cost")).append("\n");
                sb.append("   Odometer: ").append(rs.getInt("odometer_reading")).append(" km\n\n");
            }

            if (!hasRecords) {
                sb.append("No service records found for this vehicle.");
            }

            rs.close();
            pstmt.close();
            conn.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Service History");
            alert.setHeaderText(selected.getRegistrationNumber());
            alert.setContentText(sb.toString());
            alert.getDialogPane().setPrefWidth(500);
            alert.showAndWait();

        } catch (SQLException e) {
            showAlert("Error", "Failed to load history: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditService() {
        ServiceRecord selected = servicesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a service record to edit");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        Dialog<ServiceRecord> dialog = new Dialog<>();
        dialog.setTitle("Edit Service Record");
        dialog.setHeaderText("Edit service for: " + selected.getRegistrationNumber());

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField typeField = new TextField(selected.getServiceType());
        TextArea descArea = new TextArea(selected.getDescription());
        descArea.setPrefRowCount(3);
        TextField costFieldEdit = new TextField(String.valueOf(selected.getCost()));
        TextField odoFieldEdit = new TextField(String.valueOf(selected.getOdometerReading()));
        DatePicker datePicker = new DatePicker(selected.getServiceDate());

        grid.add(new Label("Service Type:"), 0, 0);
        grid.add(typeField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Cost (M):"), 0, 2);
        grid.add(costFieldEdit, 1, 2);
        grid.add(new Label("Odometer:"), 0, 3);
        grid.add(odoFieldEdit, 1, 3);
        grid.add(new Label("Service Date:"), 0, 4);
        grid.add(datePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    String sql = "UPDATE service_record SET service_type = ?, description = ?, cost = ?, odometer_reading = ?, service_date = ? WHERE service_id = ?";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, typeField.getText());
                    pstmt.setString(2, descArea.getText());
                    pstmt.setDouble(3, Double.parseDouble(costFieldEdit.getText()));
                    pstmt.setInt(4, Integer.parseInt(odoFieldEdit.getText()));
                    pstmt.setDate(5, java.sql.Date.valueOf(datePicker.getValue()));
                    pstmt.setInt(6, selected.getServiceId());
                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadServices();
                    showAlert("Success", "Service record updated!");
                } catch (Exception e) {
                    showAlert("Error", "Failed to update: " + e.getMessage());
                }
                return selected;
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteService() {
        ServiceRecord selected = servicesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a service record to delete");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete service record");
        confirm.setContentText("Delete service for " + selected.getRegistrationNumber() + "?\nThis action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String sql = "DELETE FROM service_record WHERE service_id = ?";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selected.getServiceId());
                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadServices();
                    statusLabel.setText("Service record deleted!");
                    statusLabel.setStyle("-fx-text-fill: #27ae60;");

                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> statusLabel.setText("")));
                    timeline.play();
                } catch (SQLException e) {
                    statusLabel.setText("Failed to delete: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadVehicles();
        loadServices();
        statusLabel.setText("Data refreshed!");
        statusLabel.setStyle("-fx-text-fill: #27ae60;");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> statusLabel.setText("")));
        timeline.play();
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vehicle/vehicleidentificationsystem/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("Vehicle Identification System - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}