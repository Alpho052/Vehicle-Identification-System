package com.vehicle.vehicleidentificationsystem.controller;

import com.vehicle.vehicleidentificationsystem.dao.VehicleDAO;
import com.vehicle.vehicleidentificationsystem.dao.DatabaseConnection;
import com.vehicle.vehicleidentificationsystem.model.User;
import com.vehicle.vehicleidentificationsystem.model.Vehicle;
import com.vehicle.vehicleidentificationsystem.model.InsuranceRecord;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

public class InsuranceController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<InsuranceRecord> insuranceTable;
    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private TextField companyField;
    @FXML private TextField policyNumberField;
    @FXML private TextField coverageField;
    @FXML private TextField premiumField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label statusLabel;

    private VehicleDAO vehicleDAO;
    private ObservableList<InsuranceRecord> insuranceList;
    private ObservableList<Vehicle> vehicleList;
    private User currentUser;

    private static final Pattern TEXT_PATTERN = Pattern.compile("^[A-Za-z\\s]+$");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    private static final Pattern POLICY_PATTERN = Pattern.compile("^[A-Za-z0-9-]+$");
    private static final Pattern COMPANY_PATTERN = Pattern.compile("^[A-Za-z\\s]+$");

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        insuranceList = FXCollections.observableArrayList();
        vehicleList = FXCollections.observableArrayList();

        setupTable();
        loadVehicles();
        loadInsuranceRecords();
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusYears(1));
        setupAnimations();
        setupLiveValidation();
    }

    private void setupAnimations() {
        FadeTransition fadeTable = new FadeTransition(Duration.seconds(0.5), insuranceTable);
        fadeTable.setFromValue(0);
        fadeTable.setToValue(1);
        fadeTable.play();
    }

    private void setupLiveValidation() {
        companyField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !COMPANY_PATTERN.matcher(newVal).matches()) {
                companyField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                statusLabel.setText("Company name must contain only letters");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (!newVal.isEmpty()) {
                companyField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                statusLabel.setText("");
            } else {
                companyField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        policyNumberField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !POLICY_PATTERN.matcher(newVal).matches()) {
                policyNumberField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                statusLabel.setText("Policy number can only contain letters, numbers and hyphens");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (!newVal.isEmpty()) {
                policyNumberField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                statusLabel.setText("");
            } else {
                policyNumberField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        coverageField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !AMOUNT_PATTERN.matcher(newVal).matches()) {
                coverageField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                statusLabel.setText("Coverage amount must be a valid number");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (!newVal.isEmpty()) {
                coverageField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                statusLabel.setText("");
            } else {
                coverageField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        premiumField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !AMOUNT_PATTERN.matcher(newVal).matches()) {
                premiumField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                statusLabel.setText("Premium amount must be a valid number");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (!newVal.isEmpty()) {
                premiumField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                statusLabel.setText("");
            } else {
                premiumField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });
    }

    private void setupTable() {
        insuranceTable.getColumns().clear();

        TableColumn<InsuranceRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("insuranceId"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InsuranceRecord, String> regCol = new TableColumn<>("Vehicle");
        regCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        regCol.setPrefWidth(140);

        TableColumn<InsuranceRecord, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(new PropertyValueFactory<>("insuranceCompany"));
        companyCol.setPrefWidth(150);

        TableColumn<InsuranceRecord, String> policyCol = new TableColumn<>("Policy #");
        policyCol.setCellValueFactory(new PropertyValueFactory<>("policyNumber"));
        policyCol.setPrefWidth(130);

        TableColumn<InsuranceRecord, LocalDate> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startCol.setPrefWidth(110);
        startCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InsuranceRecord, LocalDate> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        endCol.setPrefWidth(110);
        endCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InsuranceRecord, Double> coverageCol = new TableColumn<>("Coverage (M)");
        coverageCol.setCellValueFactory(new PropertyValueFactory<>("coverageAmount"));
        coverageCol.setPrefWidth(120);
        coverageCol.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<InsuranceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("currentStatus"));
        statusCol.setPrefWidth(100);
        statusCol.setStyle("-fx-alignment: CENTER;");

        insuranceTable.getColumns().addAll(idCol, regCol, companyCol, policyCol, startCol, endCol, coverageCol, statusCol);
        insuranceTable.setItems(insuranceList);
        insuranceTable.setRowFactory(tv -> {
            TableRow<InsuranceRecord> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f3e5f5;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });

        insuranceTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEditInsurance();
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

    private void loadInsuranceRecords() {
        try {
            String sql = "SELECT i.insurance_id, i.vehicle_id, v.registration_number, v.make, v.model, " +
                    "i.insurance_company, i.policy_number, i.start_date, i.end_date, " +
                    "i.coverage_amount, i.premium_amount, i.status, " +
                    "CASE WHEN i.end_date < CURRENT_DATE THEN 'EXPIRED' ELSE i.status END as current_status " +
                    "FROM insurance_record i JOIN vehicle v ON i.vehicle_id = v.vehicle_id " +
                    "ORDER BY i.start_date DESC";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            insuranceList.clear();
            while (rs.next()) {
                InsuranceRecord record = new InsuranceRecord();
                record.setInsuranceId(rs.getInt("insurance_id"));
                record.setVehicleId(rs.getInt("vehicle_id"));
                record.setRegistrationNumber(rs.getString("registration_number"));
                record.setVehicleName(rs.getString("make") + " " + rs.getString("model"));
                record.setInsuranceCompany(rs.getString("insurance_company"));
                record.setPolicyNumber(rs.getString("policy_number"));
                record.setStartDate(rs.getDate("start_date").toLocalDate());
                record.setEndDate(rs.getDate("end_date").toLocalDate());
                record.setCoverageAmount(rs.getDouble("coverage_amount"));
                record.setPremiumAmount(rs.getDouble("premium_amount"));
                record.setStatus(rs.getString("status"));
                record.setCurrentStatus(rs.getString("current_status"));
                insuranceList.add(record);
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load insurance records: " + e.getMessage());
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText(user.getFullName());
    }

    @FXML
    private void handleAddInsurance() {
        statusLabel.setText("");

        Vehicle selectedVehicle = vehicleCombo.getValue();
        String company = companyField.getText().trim();
        String policyNumber = policyNumberField.getText().trim();
        String coverageText = coverageField.getText().trim();
        String premiumText = premiumField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (selectedVehicle == null) {
            statusLabel.setText("Please select a vehicle");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (company.isEmpty()) {
            statusLabel.setText("Please enter insurance company");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!COMPANY_PATTERN.matcher(company).matches()) {
            statusLabel.setText("Company name must contain only letters");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (policyNumber.isEmpty()) {
            statusLabel.setText("Please enter policy number");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!POLICY_PATTERN.matcher(policyNumber).matches()) {
            statusLabel.setText("Policy number can only contain letters, numbers and hyphens");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (coverageText.isEmpty()) {
            statusLabel.setText("Please enter coverage amount");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!AMOUNT_PATTERN.matcher(coverageText).matches()) {
            statusLabel.setText("Please enter a valid coverage amount");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!premiumText.isEmpty() && !AMOUNT_PATTERN.matcher(premiumText).matches()) {
            statusLabel.setText("Please enter a valid premium amount");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (startDate == null) {
            statusLabel.setText("Please select start date");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (endDate == null) {
            statusLabel.setText("Please select end date");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            double coverage = Double.parseDouble(coverageText);
            double premium = premiumText.isEmpty() ? 0 : Double.parseDouble(premiumText);

            if (endDate.isBefore(startDate)) {
                statusLabel.setText("End date must be after start date");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            String checkSql = "SELECT COUNT(*) FROM insurance_record WHERE policy_number = ?";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, policyNumber);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                statusLabel.setText("Policy number already exists! Please use a unique policy number.");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                rs.close();
                checkStmt.close();
                conn.close();
                return;
            }
            rs.close();
            checkStmt.close();

            String sql = "INSERT INTO insurance_record (vehicle_id, insurance_company, policy_number, start_date, end_date, coverage_amount, premium_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedVehicle.getVehicleId());
            pstmt.setString(2, company);
            pstmt.setString(3, policyNumber);
            pstmt.setDate(4, java.sql.Date.valueOf(startDate));
            pstmt.setDate(5, java.sql.Date.valueOf(endDate));
            pstmt.setDouble(6, coverage);
            pstmt.setDouble(7, premium);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            companyField.clear();
            policyNumberField.clear();
            coverageField.clear();
            premiumField.clear();
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusYears(1));
            vehicleCombo.setValue(null);

            loadInsuranceRecords();
            statusLabel.setText("Insurance record added successfully!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setText("")));
            timeline.play();

        } catch (SQLException e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handleEditInsurance() {
        InsuranceRecord selected = insuranceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select an insurance record to edit");
            return;
        }

        Dialog<InsuranceRecord> dialog = new Dialog<>();
        dialog.setTitle("Edit Insurance");
        dialog.setHeaderText("Edit insurance for: " + selected.getRegistrationNumber());

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField companyFieldEdit = new TextField(selected.getInsuranceCompany());
        TextField policyFieldEdit = new TextField(selected.getPolicyNumber());
        TextField coverageFieldEdit = new TextField(String.valueOf(selected.getCoverageAmount()));
        TextField premiumFieldEdit = new TextField(String.valueOf(selected.getPremiumAmount()));
        DatePicker startPicker = new DatePicker(selected.getStartDate());
        DatePicker endPicker = new DatePicker(selected.getEndDate());
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("ACTIVE", "EXPIRED", "CANCELLED");
        statusCombo.setValue(selected.getStatus());

        grid.add(new Label("Company:"), 0, 0);
        grid.add(companyFieldEdit, 1, 0);
        grid.add(new Label("Policy Number:"), 0, 1);
        grid.add(policyFieldEdit, 1, 1);
        grid.add(new Label("Coverage (M):"), 0, 2);
        grid.add(coverageFieldEdit, 1, 2);
        grid.add(new Label("Premium (M):"), 0, 3);
        grid.add(premiumFieldEdit, 1, 3);
        grid.add(new Label("Start Date:"), 0, 4);
        grid.add(startPicker, 1, 4);
        grid.add(new Label("End Date:"), 0, 5);
        grid.add(endPicker, 1, 5);
        grid.add(new Label("Status:"), 0, 6);
        grid.add(statusCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    String sql = "UPDATE insurance_record SET insurance_company = ?, policy_number = ?, start_date = ?, end_date = ?, coverage_amount = ?, premium_amount = ?, status = ? WHERE insurance_id = ?";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, companyFieldEdit.getText());
                    pstmt.setString(2, policyFieldEdit.getText());
                    pstmt.setDate(3, java.sql.Date.valueOf(startPicker.getValue()));
                    pstmt.setDate(4, java.sql.Date.valueOf(endPicker.getValue()));
                    pstmt.setDouble(5, Double.parseDouble(coverageFieldEdit.getText()));
                    pstmt.setDouble(6, Double.parseDouble(premiumFieldEdit.getText()));
                    pstmt.setString(7, statusCombo.getValue());
                    pstmt.setInt(8, selected.getInsuranceId());
                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadInsuranceRecords();
                    showAlert("Success", "Insurance updated successfully!");
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
    private void handleDeleteInsurance() {
        InsuranceRecord selected = insuranceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select an insurance record to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Insurance Record");
        confirm.setContentText("Delete insurance for " + selected.getRegistrationNumber() + "?\nCompany: " + selected.getInsuranceCompany() + "\nThis action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String sql = "DELETE FROM insurance_record WHERE insurance_id = ?";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selected.getInsuranceId());
                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadInsuranceRecords();
                    showAlert("Success", "Insurance record deleted");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadVehicles();
        loadInsuranceRecords();
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