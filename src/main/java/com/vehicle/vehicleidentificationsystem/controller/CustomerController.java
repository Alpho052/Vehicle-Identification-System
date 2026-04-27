package com.vehicle.vehicleidentificationsystem.controller;

import com.vehicle.vehicleidentificationsystem.dao.QueryDAO;
import com.vehicle.vehicleidentificationsystem.dao.VehicleDAO;
import com.vehicle.vehicleidentificationsystem.dao.DatabaseConnection;
import com.vehicle.vehicleidentificationsystem.model.CustomerQuery;
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
import javafx.scene.effect.DropShadow;
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

public class CustomerController {

    @FXML private Label welcomeLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label addressLabel;
    @FXML private Label licenseNumberLabel;
    @FXML private Label dobLabel;
    @FXML private TableView<Vehicle> vehiclesTable;
    @FXML private TableView<CustomerQuery> queriesTable;
    @FXML private TextArea queryTextArea;
    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private Label statusLabel;

    private VehicleDAO vehicleDAO;
    private QueryDAO queryDAO;
    private ObservableList<Vehicle> vehicleList;
    private ObservableList<CustomerQuery> queryList;
    private User currentUser;
    private String customerAddress;
    private String customerLicense;
    private String customerDob;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\s-]{8,20}$");
    private static final Pattern REGISTRATION_PATTERN = Pattern.compile("^[A-Za-z0-9\\s-]+$");

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        queryDAO = new QueryDAO();
        vehicleList = FXCollections.observableArrayList();
        queryList = FXCollections.observableArrayList();

        setupTables();
        setupAnimations();
    }

    private void setupAnimations() {
        // Fade in animation for tables
        FadeTransition fadeVehicles = new FadeTransition(Duration.seconds(0.5), vehiclesTable);
        fadeVehicles.setFromValue(0);
        fadeVehicles.setToValue(1);
        fadeVehicles.play();

        FadeTransition fadeQueries = new FadeTransition(Duration.seconds(0.7), queriesTable);
        fadeQueries.setFromValue(0);
        fadeQueries.setToValue(1);
        fadeQueries.play();
    }

    private void setupTables() {
        vehiclesTable.getColumns().clear();

        TableColumn<Vehicle, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Vehicle, String> regCol = new TableColumn<>("Registration");
        regCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        regCol.setPrefWidth(140);

        TableColumn<Vehicle, String> makeCol = new TableColumn<>("Make");
        makeCol.setCellValueFactory(new PropertyValueFactory<>("make"));
        makeCol.setPrefWidth(100);

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        modelCol.setPrefWidth(100);

        TableColumn<Vehicle, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setPrefWidth(80);
        yearCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Vehicle, String> colorCol = new TableColumn<>("Color");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorCol.setPrefWidth(100);

        vehiclesTable.getColumns().addAll(idCol, regCol, makeCol, modelCol, yearCol, colorCol);
        vehiclesTable.setItems(vehicleList);
        vehiclesTable.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #e8f5e9;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });

        queriesTable.getColumns().clear();

        TableColumn<CustomerQuery, Integer> qidCol = new TableColumn<>("ID");
        qidCol.setCellValueFactory(new PropertyValueFactory<>("queryId"));
        qidCol.setPrefWidth(50);
        qidCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CustomerQuery, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        vehicleCol.setPrefWidth(120);

        TableColumn<CustomerQuery, String> queryCol = new TableColumn<>("Question");
        queryCol.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        queryCol.setPrefWidth(300);

        TableColumn<CustomerQuery, String> responseCol = new TableColumn<>("Response");
        responseCol.setCellValueFactory(new PropertyValueFactory<>("responseText"));
        responseCol.setPrefWidth(300);

        TableColumn<CustomerQuery, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setStyle("-fx-alignment: CENTER;");

        queriesTable.getColumns().addAll(qidCol, vehicleCol, queryCol, responseCol, statusCol);
        queriesTable.setItems(queryList);
        queriesTable.setRowFactory(tv -> {
            TableRow<CustomerQuery> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #e3f2fd;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });
    }

    private void loadData() {
        if (currentUser == null) return;

        loadCustomerProfile();

        try {
            List<Vehicle> allVehicles = vehicleDAO.getAllVehicles();
            vehicleList.clear();
            for (Vehicle v : allVehicles) {
                if (v.getOwnerId() == currentUser.getUserId()) {
                    vehicleList.add(v);
                }
            }
            vehicleCombo.setItems(vehicleList);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load vehicles: " + e.getMessage());
        }

        try {
            List<CustomerQuery> queries = queryDAO.getQueriesByCustomer(currentUser.getUserId());
            queryList.clear();
            queryList.addAll(queries);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load queries: " + e.getMessage());
        }
    }

    private void loadCustomerProfile() {
        try {
            String sql = "SELECT c.address, c.date_of_birth, c.license_number FROM customer c WHERE c.customer_id = ?";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                customerAddress = rs.getString("address");
                java.sql.Date dobDate = rs.getDate("date_of_birth");
                customerDob = dobDate != null ? dobDate.toString() : null;
                customerLicense = rs.getString("license_number");

                addressLabel.setText(customerAddress != null ? customerAddress : "Not set");
                licenseNumberLabel.setText(customerLicense != null ? customerLicense : "Not set");
                dobLabel.setText(customerDob != null ? customerDob : "Not set");
                emailLabel.setText(currentUser.getEmail());
                phoneLabel.setText(currentUser.getPhone());
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Failed to load profile: " + e.getMessage());
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText(user.getFullName());
        customerNameLabel.setText(user.getFullName());
        loadData();
    }

    private boolean validateName(String name, String fieldName) {
        if (name.isEmpty()) {
            statusLabel.setText(fieldName + " is required");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            statusLabel.setText(fieldName + " must contain only letters and spaces");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            statusLabel.setText("Email is required");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            statusLabel.setText("Please enter a valid email address");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }
        return true;
    }

    private boolean validatePhone(String phone) {
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            statusLabel.setText("Please enter a valid phone number");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }
        return true;
    }

    private boolean validateRegistration(String reg) {
        if (reg.isEmpty()) {
            statusLabel.setText("Registration number is required");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }
        if (!REGISTRATION_PATTERN.matcher(reg).matches()) {
            statusLabel.setText("Registration number can contain letters, numbers, spaces and hyphens");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return false;
        }
        return true;
    }

    @FXML
    private void handleAddVehicle() {
        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.setTitle("Add New Vehicle");
        dialog.setHeaderText("Register your vehicle");

        ButtonType addButton = new ButtonType("Add Vehicle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField regNumber = new TextField();
        regNumber.setPromptText("ABC123");
        TextField make = new TextField();
        make.setPromptText("Toyota");
        TextField model = new TextField();
        model.setPromptText("Camry");
        TextField year = new TextField();
        year.setPromptText("2020");
        TextField color = new TextField();
        color.setPromptText("Silver");

        // Live validation on registration
        regNumber.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !REGISTRATION_PATTERN.matcher(newVal).matches()) {
                regNumber.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                regNumber.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                regNumber.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        make.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !NAME_PATTERN.matcher(newVal).matches()) {
                make.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                make.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                make.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        model.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !NAME_PATTERN.matcher(newVal).matches()) {
                model.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                model.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                model.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        year.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !newVal.matches("\\d{4}")) {
                year.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                year.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                year.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        grid.add(new Label("Registration Number:*"), 0, 0);
        grid.add(regNumber, 1, 0);
        grid.add(new Label("Make:*"), 0, 1);
        grid.add(make, 1, 1);
        grid.add(new Label("Model:*"), 0, 2);
        grid.add(model, 1, 2);
        grid.add(new Label("Year:*"), 0, 3);
        grid.add(year, 1, 3);
        grid.add(new Label("Color:"), 0, 4);
        grid.add(color, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                if (!validateRegistration(regNumber.getText().trim())) return null;
                if (!validateName(make.getText().trim(), "Make")) return null;
                if (!validateName(model.getText().trim(), "Model")) return null;

                try {
                    int yearValue = Integer.parseInt(year.getText().trim());
                    if (yearValue < 1900 || yearValue > LocalDate.now().getYear() + 1) {
                        statusLabel.setText("Please enter a valid year (1900-" + (LocalDate.now().getYear() + 1) + ")");
                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                        return null;
                    }

                    String sql = "INSERT INTO vehicle (registration_number, make, model, year, owner_id, color, registration_date) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE)";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, regNumber.getText().trim().toUpperCase());
                    pstmt.setString(2, make.getText().trim());
                    pstmt.setString(3, model.getText().trim());
                    pstmt.setInt(4, yearValue);
                    pstmt.setInt(5, currentUser.getUserId());
                    pstmt.setString(6, color.getText().trim());
                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadData();
                    statusLabel.setText("Vehicle added successfully!");
                    statusLabel.setStyle("-fx-text-fill: #27ae60;");

                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setText("")));
                    timeline.play();
                } catch (NumberFormatException e) {
                    statusLabel.setText("Please enter a valid year");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                } catch (SQLException e) {
                    statusLabel.setText("Registration number may already exist");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
                return null;
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleEditVehicle() {
        Vehicle selected = vehiclesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a vehicle to edit");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.setTitle("Edit Vehicle");
        dialog.setHeaderText("Edit vehicle: " + selected.getRegistrationNumber());

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField regNumber = new TextField(selected.getRegistrationNumber());
        TextField make = new TextField(selected.getMake());
        TextField model = new TextField(selected.getModel());
        TextField year = new TextField(String.valueOf(selected.getYear()));
        TextField color = new TextField(selected.getColor());

        grid.add(new Label("Registration Number:*"), 0, 0);
        grid.add(regNumber, 1, 0);
        grid.add(new Label("Make:*"), 0, 1);
        grid.add(make, 1, 1);
        grid.add(new Label("Model:*"), 0, 2);
        grid.add(model, 1, 2);
        grid.add(new Label("Year:*"), 0, 3);
        grid.add(year, 1, 3);
        grid.add(new Label("Color:"), 0, 4);
        grid.add(color, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                if (!validateRegistration(regNumber.getText().trim())) return null;
                if (!validateName(make.getText().trim(), "Make")) return null;
                if (!validateName(model.getText().trim(), "Model")) return null;

                try {
                    int yearValue = Integer.parseInt(year.getText().trim());
                    String sql = "UPDATE vehicle SET registration_number = ?, make = ?, model = ?, year = ?, color = ? WHERE vehicle_id = ?";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, regNumber.getText().trim().toUpperCase());
                    pstmt.setString(2, make.getText().trim());
                    pstmt.setString(3, model.getText().trim());
                    pstmt.setInt(4, yearValue);
                    pstmt.setString(5, color.getText().trim());
                    pstmt.setInt(6, selected.getVehicleId());
                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadData();
                    statusLabel.setText("Vehicle updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: #27ae60;");

                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setText("")));
                    timeline.play();
                } catch (Exception e) {
                    statusLabel.setText("Failed to update vehicle");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
                return null;
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteVehicle() {
        Vehicle selected = vehiclesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a vehicle to delete");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete vehicle: " + selected.getRegistrationNumber());
        confirm.setContentText("This action cannot be undone. Are you sure?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String sql = "DELETE FROM vehicle WHERE vehicle_id = ?";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selected.getVehicleId());
                    pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    loadData();
                    statusLabel.setText("Vehicle deleted successfully!");
                    statusLabel.setStyle("-fx-text-fill: #27ae60;");

                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setText("")));
                    timeline.play();
                } catch (SQLException e) {
                    statusLabel.setText("Failed to delete vehicle");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
            }
        });
    }

    @FXML
    private void handleEditProfile() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your personal information");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField fullNameField = new TextField(currentUser.getFullName());
        TextField emailField = new TextField(currentUser.getEmail());
        TextField phoneField = new TextField(currentUser.getPhone());
        TextField addressField = new TextField(customerAddress != null ? customerAddress : "");
        TextField licenseField = new TextField(customerLicense != null ? customerLicense : "");
        DatePicker dobPicker = new DatePicker();
        if (customerDob != null && !customerDob.isEmpty()) {
            try {
                dobPicker.setValue(LocalDate.parse(customerDob));
            } catch (Exception e) {}
        }

        // Live validation
        fullNameField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !NAME_PATTERN.matcher(newVal).matches()) {
                fullNameField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                fullNameField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                fullNameField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        emailField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !EMAIL_PATTERN.matcher(newVal).matches()) {
                emailField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                emailField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                emailField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        phoneField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !PHONE_PATTERN.matcher(newVal).matches()) {
                phoneField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                phoneField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                phoneField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        grid.add(new Label("Full Name:*"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Email:*"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);
        grid.add(new Label("License Number:"), 0, 4);
        grid.add(licenseField, 1, 4);
        grid.add(new Label("Date of Birth:"), 0, 5);
        grid.add(dobPicker, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                if (!validateName(fullNameField.getText().trim(), "Full Name")) return null;
                if (!validateEmail(emailField.getText().trim())) return null;
                if (!validatePhone(phoneField.getText().trim())) return null;

                try {
                    Connection conn = DatabaseConnection.getInstance().getConnection();

                    String sql1 = "UPDATE users SET full_name = ?, email = ?, phone = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
                    PreparedStatement pstmt1 = conn.prepareStatement(sql1);
                    pstmt1.setString(1, fullNameField.getText().trim());
                    pstmt1.setString(2, emailField.getText().trim());
                    pstmt1.setString(3, phoneField.getText().trim());
                    pstmt1.setInt(4, currentUser.getUserId());
                    pstmt1.executeUpdate();
                    pstmt1.close();

                    String sql2 = "UPDATE customer SET address = ?, license_number = ?, date_of_birth = ? WHERE customer_id = ?";
                    PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                    pstmt2.setString(1, addressField.getText().trim());
                    pstmt2.setString(2, licenseField.getText().trim());
                    java.sql.Date sqlDate = dobPicker.getValue() != null ? java.sql.Date.valueOf(dobPicker.getValue()) : null;
                    pstmt2.setDate(3, sqlDate);
                    pstmt2.setInt(4, currentUser.getUserId());
                    pstmt2.executeUpdate();
                    pstmt2.close();

                    conn.close();

                    currentUser.setFullName(fullNameField.getText().trim());
                    currentUser.setEmail(emailField.getText().trim());
                    currentUser.setPhone(phoneField.getText().trim());

                    loadCustomerProfile();
                    welcomeLabel.setText(currentUser.getFullName());
                    customerNameLabel.setText(currentUser.getFullName());

                    statusLabel.setText("Profile updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: #27ae60;");

                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setText("")));
                    timeline.play();
                } catch (SQLException e) {
                    statusLabel.setText("Failed to update profile");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
                return null;
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleSubmitQuery() {
        String queryText = queryTextArea.getText().trim();
        Vehicle selectedVehicle = vehicleCombo.getValue();

        if (queryText.isEmpty()) {
            statusLabel.setText("Please enter your question");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (selectedVehicle == null) {
            statusLabel.setText("Please select a vehicle");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            CustomerQuery query = new CustomerQuery();
            query.setCustomerId(currentUser.getUserId());
            query.setVehicleId(selectedVehicle.getVehicleId());
            query.setQueryText(queryText);

            queryDAO.createQuery(query);
            queryTextArea.clear();
            loadData();
            statusLabel.setText("Query submitted successfully!");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> statusLabel.setText("")));
            timeline.play();

        } catch (SQLException e) {
            statusLabel.setText("Failed to submit query");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
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