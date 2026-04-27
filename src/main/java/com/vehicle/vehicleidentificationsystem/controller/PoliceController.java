package com.vehicle.vehicleidentificationsystem.controller;

import com.vehicle.vehicleidentificationsystem.dao.VehicleDAO;
import com.vehicle.vehicleidentificationsystem.dao.DatabaseConnection;
import com.vehicle.vehicleidentificationsystem.model.PoliceReport;
import com.vehicle.vehicleidentificationsystem.model.User;
import com.vehicle.vehicleidentificationsystem.model.Vehicle;
import com.vehicle.vehicleidentificationsystem.model.Violation;
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

public class PoliceController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<PoliceReport> reportsTable;
    @FXML private TableView<Violation> violationsTable;
    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private ComboBox<Vehicle> vehicleComboViolation;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private TextArea reportDescArea;
    @FXML private TextField locationField;
    @FXML private TextField caseNumberField;
    @FXML private TextField violationTypeField;
    @FXML private TextField fineAmountField;
    @FXML private TextField pointsField;
    @FXML private DatePicker violationDatePicker;
    @FXML private Label statusLabel;

    private VehicleDAO vehicleDAO;
    private ObservableList<PoliceReport> reportList;
    private ObservableList<Violation> violationList;
    private ObservableList<Vehicle> vehicleList;
    private User currentUser;

    private static final Pattern TEXT_PATTERN = Pattern.compile("^[A-Za-z\\s]+$");
    private static final Pattern FINE_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    private static final Pattern CASE_PATTERN = Pattern.compile("^[A-Za-z0-9-]+$");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^[A-Za-z0-9\\s,.-]+$");

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        reportList = FXCollections.observableArrayList();
        violationList = FXCollections.observableArrayList();
        vehicleList = FXCollections.observableArrayList();

        reportTypeCombo.getItems().addAll("ACCIDENT", "THEFT", "VIOLATION");
        violationDatePicker.setValue(LocalDate.now());

        setupTables();
        loadVehicles();
        loadReports();
        loadViolations();
        setupAnimations();
        setupLiveValidation();
    }

    private void setupAnimations() {
        FadeTransition fadeReports = new FadeTransition(Duration.seconds(0.5), reportsTable);
        fadeReports.setFromValue(0);
        fadeReports.setToValue(1);
        fadeReports.play();

        FadeTransition fadeViolations = new FadeTransition(Duration.seconds(0.7), violationsTable);
        fadeViolations.setFromValue(0);
        fadeViolations.setToValue(1);
        fadeViolations.play();
    }

    private void setupLiveValidation() {
        fineAmountField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !FINE_PATTERN.matcher(newVal).matches()) {
                fineAmountField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                statusLabel.setText("Fine amount must be a valid number");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (!newVal.isEmpty()) {
                fineAmountField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                statusLabel.setText("");
            } else {
                fineAmountField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        caseNumberField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !CASE_PATTERN.matcher(newVal).matches()) {
                caseNumberField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                statusLabel.setText("Case number can only contain letters, numbers and hyphens");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (!newVal.isEmpty()) {
                caseNumberField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                statusLabel.setText("");
            } else {
                caseNumberField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        violationTypeField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !TEXT_PATTERN.matcher(newVal).matches()) {
                violationTypeField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                statusLabel.setText("Violation type must contain only letters");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            } else if (!newVal.isEmpty()) {
                violationTypeField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
                statusLabel.setText("");
            } else {
                violationTypeField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        locationField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !LOCATION_PATTERN.matcher(newVal).matches()) {
                locationField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                locationField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                locationField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        reportDescArea.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty()) {
                reportDescArea.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                reportDescArea.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });
    }

    private void setupTables() {
        reportsTable.getColumns().clear();

        TableColumn<PoliceReport, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PoliceReport, String> regCol = new TableColumn<>("Vehicle");
        regCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        regCol.setPrefWidth(140);

        TableColumn<PoliceReport, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        typeCol.setPrefWidth(100);
        typeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PoliceReport, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(300);

        TableColumn<PoliceReport, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(150);

        TableColumn<PoliceReport, String> caseCol = new TableColumn<>("Case #");
        caseCol.setCellValueFactory(new PropertyValueFactory<>("caseNumber"));
        caseCol.setPrefWidth(120);

        TableColumn<PoliceReport, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        statusCol.setStyle("-fx-alignment: CENTER;");

        reportsTable.getColumns().addAll(idCol, regCol, typeCol, descCol, locationCol, caseCol, statusCol);
        reportsTable.setItems(reportList);
        reportsTable.setRowFactory(tv -> {
            TableRow<PoliceReport> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #ffebee;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });

        violationsTable.getColumns().clear();

        TableColumn<Violation, Integer> vidCol = new TableColumn<>("ID");
        vidCol.setCellValueFactory(new PropertyValueFactory<>("violationId"));
        vidCol.setPrefWidth(50);
        vidCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Violation, String> vregCol = new TableColumn<>("Vehicle");
        vregCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        vregCol.setPrefWidth(140);

        TableColumn<Violation, String> vtypeCol = new TableColumn<>("Violation");
        vtypeCol.setCellValueFactory(new PropertyValueFactory<>("violationType"));
        vtypeCol.setPrefWidth(150);

        TableColumn<Violation, Double> fineCol = new TableColumn<>("Fine (M)");
        fineCol.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        fineCol.setPrefWidth(100);
        fineCol.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<Violation, Integer> pointsCol = new TableColumn<>("Points");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        pointsCol.setPrefWidth(80);
        pointsCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Violation, Boolean> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("paid"));
        paidCol.setPrefWidth(80);
        paidCol.setStyle("-fx-alignment: CENTER;");

        violationsTable.getColumns().addAll(vidCol, vregCol, vtypeCol, fineCol, pointsCol, paidCol);
        violationsTable.setItems(violationList);
        violationsTable.setRowFactory(tv -> {
            TableRow<Violation> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #ffebee;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });
    }

    private void loadVehicles() {
        try {
            List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
            vehicleList.clear();
            vehicleList.addAll(vehicles);
            vehicleCombo.setItems(vehicleList);
            vehicleComboViolation.setItems(vehicleList);
            vehicleCombo.setPromptText("Select a vehicle");
            vehicleComboViolation.setPromptText("Select a vehicle");
        } catch (SQLException e) {
            showAlert("Error", "Failed to load vehicles: " + e.getMessage());
        }
    }

    private void loadReports() {
        try {
            String sql = "SELECT pr.report_id, pr.vehicle_id, v.registration_number, pr.report_date, pr.report_type, pr.description, pr.location, pr.case_number, pr.status FROM police_report pr JOIN vehicle v ON pr.vehicle_id = v.vehicle_id ORDER BY pr.report_date DESC";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            reportList.clear();
            while (rs.next()) {
                PoliceReport r = new PoliceReport();
                r.setReportId(rs.getInt("report_id"));
                r.setVehicleId(rs.getInt("vehicle_id"));
                r.setRegistrationNumber(rs.getString("registration_number"));
                r.setReportDate(rs.getTimestamp("report_date").toLocalDateTime());
                r.setReportType(rs.getString("report_type"));
                r.setDescription(rs.getString("description"));
                r.setLocation(rs.getString("location"));
                r.setCaseNumber(rs.getString("case_number"));
                r.setStatus(rs.getString("status"));
                reportList.add(r);
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load reports: " + e.getMessage());
        }
    }

    private void loadViolations() {
        try {
            String sql = "SELECT v.violation_id, v.vehicle_id, vh.registration_number, v.violation_date, v.violation_type, v.fine_amount, v.points, v.is_paid FROM violation v JOIN vehicle vh ON v.vehicle_id = vh.vehicle_id ORDER BY v.violation_date DESC";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            violationList.clear();
            while (rs.next()) {
                Violation v = new Violation();
                v.setViolationId(rs.getInt("violation_id"));
                v.setVehicleId(rs.getInt("vehicle_id"));
                v.setRegistrationNumber(rs.getString("registration_number"));
                v.setViolationDate(rs.getDate("violation_date").toLocalDate());
                v.setViolationType(rs.getString("violation_type"));
                v.setFineAmount(rs.getDouble("fine_amount"));
                v.setPoints(rs.getInt("points"));
                v.setPaid(rs.getBoolean("is_paid"));
                violationList.add(v);
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load violations: " + e.getMessage());
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText(user.getFullName());
    }

    private boolean caseNumberExists(String caseNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM police_report WHERE case_number = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, caseNumber);
        ResultSet rs = pstmt.executeQuery();
        boolean exists = rs.next() && rs.getInt(1) > 0;
        rs.close();
        pstmt.close();
        conn.close();
        return exists;
    }

    @FXML
    private void handleAddReport() {
        statusLabel.setText("");

        Vehicle selectedVehicle = vehicleCombo.getValue();
        String reportType = reportTypeCombo.getValue();
        String description = reportDescArea.getText().trim();
        String location = locationField.getText().trim();
        String caseNumber = caseNumberField.getText().trim();

        if (selectedVehicle == null) {
            statusLabel.setText("Please select a vehicle");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (reportType == null) {
            statusLabel.setText("Please select report type");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (description.isEmpty()) {
            statusLabel.setText("Please enter description");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (caseNumber.isEmpty()) {
            statusLabel.setText("Please enter case number");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!CASE_PATTERN.matcher(caseNumber).matches()) {
            statusLabel.setText("Case number can only contain letters, numbers and hyphens");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            if (caseNumberExists(caseNumber)) {
                statusLabel.setText("Case number already exists! Please use a unique case number.");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            String sql = "INSERT INTO police_report (vehicle_id, officer_id, report_type, description, location, case_number, status) VALUES (?, ?, ?::report_type, ?, ?, ?, 'OPEN')";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedVehicle.getVehicleId());
            pstmt.setInt(2, currentUser.getUserId());
            pstmt.setString(3, reportType);
            pstmt.setString(4, description);
            pstmt.setString(5, location);
            pstmt.setString(6, caseNumber);
            pstmt.executeUpdate();
            pstmt.close();

            if ("THEFT".equals(reportType)) {
                String updateSql = "UPDATE vehicle SET is_stolen = true WHERE vehicle_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, selectedVehicle.getVehicleId());
                updateStmt.executeUpdate();
                updateStmt.close();
            }
            conn.close();

            reportDescArea.clear();
            locationField.clear();
            caseNumberField.clear();
            reportTypeCombo.setValue(null);
            vehicleCombo.setValue(null);

            loadReports();
            statusLabel.setText("Police report added successfully!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setText("")));
            timeline.play();

        } catch (SQLException e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handleAddViolation() {
        statusLabel.setText("");

        Vehicle selectedVehicle = vehicleComboViolation.getValue();
        String violationType = violationTypeField.getText().trim();
        String fineText = fineAmountField.getText().trim();
        String pointsText = pointsField.getText().trim();
        LocalDate violationDate = violationDatePicker.getValue();

        if (selectedVehicle == null) {
            statusLabel.setText("Please select a vehicle");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (violationType.isEmpty()) {
            statusLabel.setText("Please enter violation type");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!TEXT_PATTERN.matcher(violationType).matches()) {
            statusLabel.setText("Violation type must contain only letters");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (fineText.isEmpty()) {
            statusLabel.setText("Please enter fine amount");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (!FINE_PATTERN.matcher(fineText).matches()) {
            statusLabel.setText("Please enter a valid fine amount (e.g., 500.00)");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (violationDate == null) {
            statusLabel.setText("Please select violation date");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            double fine = Double.parseDouble(fineText);
            int points = pointsText.isEmpty() ? 0 : Integer.parseInt(pointsText);

            String sql = "INSERT INTO violation (vehicle_id, violation_date, violation_type, fine_amount, points, is_paid) VALUES (?, ?, ?, ?, ?, false)";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedVehicle.getVehicleId());
            pstmt.setDate(2, java.sql.Date.valueOf(violationDate));
            pstmt.setString(3, violationType);
            pstmt.setDouble(4, fine);
            pstmt.setInt(5, points);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();

            violationTypeField.clear();
            fineAmountField.clear();
            pointsField.clear();
            violationDatePicker.setValue(LocalDate.now());
            vehicleComboViolation.setValue(null);

            loadViolations();
            statusLabel.setText("Violation recorded successfully!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setText("")));
            timeline.play();

        } catch (SQLException e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handleViewVehicleHistory() {
        Vehicle selected = vehicleCombo.getValue();
        if (selected == null) {
            showAlert("Info", "Please select a vehicle from the dropdown to view its history");
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════════════════════\n");
            sb.append("                    VEHICLE HISTORY REPORT\n");
            sb.append("═══════════════════════════════════════════════════════════════════════════\n\n");
            sb.append("VEHICLE INFORMATION\n");
            sb.append("───────────────────────────────────────────────────────────────────────────\n");
            sb.append("  Registration: ").append(selected.getRegistrationNumber()).append("\n");
            sb.append("  Make/Model:   ").append(selected.getMake()).append(" ").append(selected.getModel()).append("\n");
            sb.append("  Year:         ").append(selected.getYear()).append("\n");
            sb.append("  Color:        ").append(selected.getColor() != null ? selected.getColor() : "N/A").append("\n");
            sb.append("  Stolen Status: ").append(selected.isStolen() ? "REPORTED STOLEN" : "CLEAR").append("\n");
            sb.append("\n");

            Connection conn = DatabaseConnection.getInstance().getConnection();

            String reportSql = "SELECT * FROM police_report WHERE vehicle_id = ? ORDER BY report_date DESC";
            PreparedStatement reportStmt = conn.prepareStatement(reportSql);
            reportStmt.setInt(1, selected.getVehicleId());
            ResultSet reportRs = reportStmt.executeQuery();

            sb.append("POLICE REPORTS\n");
            sb.append("───────────────────────────────────────────────────────────────────────────\n");
            boolean hasReports = false;
            int reportCount = 0;
            while (reportRs.next()) {
                hasReports = true;
                reportCount++;
                sb.append("\n  REPORT #").append(reportCount).append("\n");
                sb.append("     Type:        ").append(reportRs.getString("report_type")).append("\n");
                sb.append("     Date:        ").append(reportRs.getTimestamp("report_date")).append("\n");
                sb.append("     Case #:      ").append(reportRs.getString("case_number")).append("\n");
                sb.append("     Location:    ").append(reportRs.getString("location")).append("\n");
                sb.append("     Status:      ").append(reportRs.getString("status")).append("\n");
                sb.append("     Description: ").append(reportRs.getString("description")).append("\n");
            }
            if (!hasReports) {
                sb.append("\n  No police reports found for this vehicle.\n");
            }
            reportRs.close();
            reportStmt.close();

            String violationSql = "SELECT * FROM violation WHERE vehicle_id = ? ORDER BY violation_date DESC";
            PreparedStatement violationStmt = conn.prepareStatement(violationSql);
            violationStmt.setInt(1, selected.getVehicleId());
            ResultSet violationRs = violationStmt.executeQuery();

            sb.append("\nTRAFFIC VIOLATIONS\n");
            sb.append("───────────────────────────────────────────────────────────────────────────\n");
            boolean hasViolations = false;
            int violationCount = 0;
            while (violationRs.next()) {
                hasViolations = true;
                violationCount++;
                sb.append("\n  VIOLATION #").append(violationCount).append("\n");
                sb.append("     Type:        ").append(violationRs.getString("violation_type")).append("\n");
                sb.append("     Date:        ").append(violationRs.getDate("violation_date")).append("\n");
                sb.append("     Fine:        M").append(violationRs.getDouble("fine_amount")).append("\n");
                sb.append("     Points:      ").append(violationRs.getInt("points")).append("\n");
                sb.append("     Status:      ").append(violationRs.getBoolean("is_paid") ? "PAID" : "UNPAID").append("\n");
            }
            if (!hasViolations) {
                sb.append("\n  No traffic violations found for this vehicle.\n");
            }
            violationRs.close();
            violationStmt.close();
            conn.close();

            sb.append("\n═══════════════════════════════════════════════════════════════════════════\n");
            sb.append("                    END OF REPORT\n");
            sb.append("═══════════════════════════════════════════════════════════════════════════\n");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Vehicle History Report");
            alert.setHeaderText(selected.getRegistrationNumber());
            alert.setContentText(sb.toString());
            alert.getDialogPane().setPrefWidth(700);
            alert.getDialogPane().setPrefHeight(550);
            alert.showAndWait();

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load history: " + e.getMessage());
        }
    }

    @FXML
    private void handleMarkViolationPaid() {
        Violation selected = violationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a violation to mark as paid");
            return;
        }

        if (selected.isPaid()) {
            showAlert("Info", "This violation is already marked as paid");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Payment");
        confirm.setHeaderText("Mark Violation as Paid");
        confirm.setContentText("Vehicle: " + selected.getRegistrationNumber() +
                "\nViolation: " + selected.getViolationType() +
                "\nFine: M" + selected.getFineAmount() +
                "\n\nMark this violation as paid?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String sql = "UPDATE violation SET is_paid = true, payment_date = CURRENT_DATE WHERE violation_id = ?";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selected.getViolationId());
                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadViolations();
                    showAlert("Success", "Violation marked as paid!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to update: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleDeleteReport() {
        PoliceReport selected = reportsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a report to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Police Report");
        confirm.setContentText("Case #: " + selected.getCaseNumber() +
                "\nType: " + selected.getReportType() +
                "\nVehicle: " + selected.getRegistrationNumber() +
                "\n\nThis action cannot be undone!");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String sql = "DELETE FROM police_report WHERE report_id = ?";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selected.getReportId());
                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadReports();
                    showAlert("Success", "Report deleted successfully!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadVehicles();
        loadReports();
        loadViolations();
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