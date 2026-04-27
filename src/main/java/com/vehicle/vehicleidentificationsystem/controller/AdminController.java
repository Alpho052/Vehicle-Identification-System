package com.vehicle.vehicleidentificationsystem.controller;

import com.vehicle.vehicleidentificationsystem.dao.UserDAO;
import com.vehicle.vehicleidentificationsystem.dao.VehicleDAO;
import com.vehicle.vehicleidentificationsystem.dao.QueryDAO;
import com.vehicle.vehicleidentificationsystem.dao.DatabaseConnection;
import com.vehicle.vehicleidentificationsystem.model.User;
import com.vehicle.vehicleidentificationsystem.model.Vehicle;
import com.vehicle.vehicleidentificationsystem.model.CustomerQuery;
import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class AdminController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalVehiclesLabel;
    @FXML private Label activeSessionsLabel;
    @FXML private TableView<User> usersTable;
    @FXML private TableView<Vehicle> vehiclesTable;
    @FXML private TableView<CustomerQuery> queriesTable;
    @FXML private ProgressBar systemProgress;
    @FXML private ProgressIndicator systemIndicator;
    @FXML private VBox cardTotalUsers;
    @FXML private VBox cardActiveUsers;
    @FXML private VBox cardTotalVehicles;

    private UserDAO userDAO;
    private VehicleDAO vehicleDAO;
    private QueryDAO queryDAO;
    private ObservableList<User> userList;
    private ObservableList<Vehicle> vehicleList;
    private ObservableList<CustomerQuery> queryList;
    private User currentUser;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\s-]{8,20}$");

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        vehicleDAO = new VehicleDAO();
        queryDAO = new QueryDAO();
        userList = FXCollections.observableArrayList();
        vehicleList = FXCollections.observableArrayList();
        queryList = FXCollections.observableArrayList();

        setupTables();
        loadDashboardData();
        loadQueries();
        setupCardAnimations();
        setupProgressAnimation();
    }

    private void setupCardAnimations() {
        // Slide in from left
        TranslateTransition slide1 = new TranslateTransition(Duration.seconds(0.5), cardTotalUsers);
        slide1.setFromX(-100);
        slide1.setToX(0);
        slide1.play();

        TranslateTransition slide2 = new TranslateTransition(Duration.seconds(0.6), cardActiveUsers);
        slide2.setFromX(-100);
        slide2.setToX(0);
        slide2.play();

        TranslateTransition slide3 = new TranslateTransition(Duration.seconds(0.7), cardTotalVehicles);
        slide3.setFromX(-100);
        slide3.setToX(0);
        slide3.play();

        // Hover effects on cards
        cardTotalUsers.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), cardTotalUsers);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
            cardTotalUsers.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        });
        cardTotalUsers.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), cardTotalUsers);
            st.setToX(1);
            st.setToY(1);
            st.play();
            cardTotalUsers.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        });

        cardActiveUsers.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), cardActiveUsers);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
            cardActiveUsers.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        });
        cardActiveUsers.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), cardActiveUsers);
            st.setToX(1);
            st.setToY(1);
            st.play();
            cardActiveUsers.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        });

        cardTotalVehicles.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), cardTotalVehicles);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
            cardTotalVehicles.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        });
        cardTotalVehicles.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), cardTotalVehicles);
            st.setToX(1);
            st.setToY(1);
            st.play();
            cardTotalVehicles.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        });
    }

    private void setupProgressAnimation() {
        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), systemIndicator);
        ft.setFromValue(0.5);
        ft.setToValue(1.0);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setAutoReverse(true);
        ft.play();

        RotateTransition rt = new RotateTransition(Duration.seconds(2), systemIndicator);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.play();
    }

    private void animateCountUp(Label label, int targetValue) {
        Timeline timeline = new Timeline();
        IntegerProperty counter = new SimpleIntegerProperty(0);
        counter.addListener((obs, old, newVal) -> label.setText(String.valueOf(newVal)));
        KeyFrame keyFrame = new KeyFrame(Duration.millis(20), e -> {
            if (counter.get() < targetValue) {
                counter.set(counter.get() + 1);
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(targetValue);
        timeline.play();
    }

    private void setupTables() {
        usersTable.getColumns().clear();

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(120);

        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(180);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);
        roleCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);

        TableColumn<User, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(70);
        activeCol.setStyle("-fx-alignment: CENTER;");

        usersTable.getColumns().addAll(idCol, usernameCol, nameCol, roleCol, emailCol, phoneCol, activeCol);
        usersTable.setItems(userList);
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0f0f0;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });

        usersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEditUser();
            }
        });

        vehiclesTable.getColumns().clear();

        TableColumn<Vehicle, Integer> vidCol = new TableColumn<>("ID");
        vidCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        vidCol.setPrefWidth(50);
        vidCol.setStyle("-fx-alignment: CENTER;");

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

        TableColumn<Vehicle, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        ownerCol.setPrefWidth(150);

        vehiclesTable.getColumns().addAll(vidCol, regCol, makeCol, modelCol, yearCol, ownerCol);
        vehiclesTable.setItems(vehicleList);
        vehiclesTable.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0f0f0;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });

        queriesTable.getColumns().clear();

        TableColumn<CustomerQuery, Integer> qidCol = new TableColumn<>("ID");
        qidCol.setCellValueFactory(new PropertyValueFactory<>("queryId"));
        qidCol.setPrefWidth(50);
        qidCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CustomerQuery, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerCol.setPrefWidth(150);

        TableColumn<CustomerQuery, String> vehicleColQ = new TableColumn<>("Vehicle");
        vehicleColQ.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        vehicleColQ.setPrefWidth(120);

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

        queriesTable.getColumns().addAll(qidCol, customerCol, vehicleColQ, queryCol, responseCol, statusCol);
        queriesTable.setItems(queryList);
        queriesTable.setRowFactory(tv -> {
            TableRow<CustomerQuery> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0f0f0;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });

        queriesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleRespondToQuery();
            }
        });
    }

    private void loadDashboardData() {
        loadUsers();
        loadVehicles();

        try {
            List<User> users = userDAO.getAllUsers();
            int total = users.size();
            long activeCount = users.stream().filter(User::isActive).count();
            animateCountUp(totalUsersLabel, total);
            animateCountUp(activeSessionsLabel, (int) activeCount);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load data: " + e.getMessage());
        }

        try {
            List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
            animateCountUp(totalVehiclesLabel, vehicles.size());
        } catch (SQLException e) {
            showAlert("Error", "Failed to load vehicles: " + e.getMessage());
        }
    }

    private void loadUsers() {
        try {
            List<User> users = userDAO.getAllUsers();
            userList.clear();
            userList.addAll(users);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void loadVehicles() {
        try {
            List<Vehicle> vehicles = vehicleDAO.getAllVehicles();
            vehicleList.clear();
            vehicleList.addAll(vehicles);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load vehicles: " + e.getMessage());
        }
    }

    private void loadQueries() {
        try {
            String sql = "SELECT q.query_id, q.customer_id, u.full_name as customer_name, q.vehicle_id, v.registration_number, q.query_date, q.query_text, q.response_text, q.status, q.response_date FROM customer_query q JOIN customer c ON q.customer_id = c.customer_id JOIN users u ON c.customer_id = u.user_id LEFT JOIN vehicle v ON q.vehicle_id = v.vehicle_id ORDER BY q.query_date DESC";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            queryList.clear();
            while (rs.next()) {
                CustomerQuery q = new CustomerQuery();
                q.setQueryId(rs.getInt("query_id"));
                q.setCustomerId(rs.getInt("customer_id"));
                q.setCustomerName(rs.getString("customer_name"));
                q.setVehicleId(rs.getInt("vehicle_id"));
                q.setRegistrationNumber(rs.getString("registration_number"));
                q.setQueryDate(rs.getTimestamp("query_date").toLocalDateTime());
                q.setQueryText(rs.getString("query_text"));
                q.setResponseText(rs.getString("response_text"));
                q.setStatus(rs.getString("status"));
                queryList.add(q);
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load queries: " + e.getMessage());
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText(user.getFullName());
    }

    private boolean validateName(String name, String fieldName) {
        if (name.isEmpty()) {
            showAlert("Validation Error", fieldName + " is required");
            return false;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            showAlert("Validation Error", fieldName + " must contain only letters and spaces");
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            showAlert("Validation Error", "Email is required");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert("Validation Error", "Please enter a valid email address");
            return false;
        }
        return true;
    }

    private boolean validatePhone(String phone) {
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showAlert("Validation Error", "Please enter a valid phone number");
            return false;
        }
        return true;
    }

    @FXML
    private void handleCreateUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Enter user details");

        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        TextField fullName = new TextField();
        fullName.setPromptText("Full Name");
        TextField email = new TextField();
        email.setPromptText("Email");
        TextField phone = new TextField();
        phone.setPromptText("Phone");
        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("ADMIN", "CUSTOMER", "POLICE", "WORKSHOP", "INSURANCE");
        role.setValue("CUSTOMER");
        TextField address = new TextField();
        address.setPromptText("Address (for customers)");

        // Live validation listeners
        fullName.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !NAME_PATTERN.matcher(newVal).matches()) {
                fullName.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                fullName.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                fullName.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        email.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !EMAIL_PATTERN.matcher(newVal).matches()) {
                email.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                email.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                email.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        phone.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty() && !PHONE_PATTERN.matcher(newVal).matches()) {
                phone.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else if (!newVal.isEmpty()) {
                phone.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2;");
            } else {
                phone.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
            }
        });

        grid.add(new Label("Username:*"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:*"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Full Name:*"), 0, 2);
        grid.add(fullName, 1, 2);
        grid.add(new Label("Email:*"), 0, 3);
        grid.add(email, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(phone, 1, 4);
        grid.add(new Label("Role:*"), 0, 5);
        grid.add(role, 1, 5);
        grid.add(new Label("Address:"), 0, 6);
        grid.add(address, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButton) {
                if (username.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Username is required");
                    return null;
                }
                if (password.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Password is required");
                    return null;
                }
                if (!validateName(fullName.getText().trim(), "Full Name")) return null;
                if (!validateEmail(email.getText().trim())) return null;
                if (!validatePhone(phone.getText().trim())) return null;

                try {
                    User user = new User();
                    user.setUsername(username.getText().trim());
                    user.setPassword(password.getText().trim());
                    user.setFullName(fullName.getText().trim());
                    user.setEmail(email.getText().trim());
                    user.setPhone(phone.getText().trim());
                    user.setRole(role.getValue());

                    int result = userDAO.createUser(user, address.getText().trim());
                    if (result > 0) {
                        loadUsers();
                        loadDashboardData();
                        showSuccessAlert("User created successfully!");
                    } else {
                        showAlert("Error", "Failed to create user");
                    }
                } catch (SQLException e) {
                    showAlert("Error", "Username or email already exists");
                }
                return null;
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleEditUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a user to edit");
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + selected.getFullName());

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField(selected.getUsername());
        TextField fullNameField = new TextField(selected.getFullName());
        TextField emailField = new TextField(selected.getEmail());
        TextField phoneField = new TextField(selected.getPhone());
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "CUSTOMER", "POLICE", "WORKSHOP", "INSURANCE");
        roleCombo.setValue(selected.getRole());
        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(selected.isActive());

        grid.add(new Label("Username:*"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Full Name:*"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Email:*"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Role:*"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(activeCheck, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                if (!validateName(fullNameField.getText().trim(), "Full Name")) return null;
                if (!validateEmail(emailField.getText().trim())) return null;
                if (!validatePhone(phoneField.getText().trim())) return null;

                try {
                    selected.setUsername(usernameField.getText().trim());
                    selected.setFullName(fullNameField.getText().trim());
                    selected.setEmail(emailField.getText().trim());
                    selected.setPhone(phoneField.getText().trim());
                    selected.setRole(roleCombo.getValue());
                    selected.setActive(activeCheck.isSelected());

                    boolean success = userDAO.updateUser(selected);
                    if (success) {
                        loadUsers();
                        loadDashboardData();
                        showSuccessAlert("User updated successfully!");
                    } else {
                        showAlert("Error", "Failed to update user");
                    }
                } catch (SQLException e) {
                    showAlert("Error", "Failed to update user: " + e.getMessage());
                }
                return null;
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a user to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete user: " + selected.getFullName());
        confirm.setContentText("This action cannot be undone. Are you sure?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userDAO.deleteUser(selected.getUserId());
                    loadUsers();
                    loadDashboardData();
                    showSuccessAlert("User deleted successfully!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete user: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRespondToQuery() {
        CustomerQuery selected = queriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a query to respond to");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Respond to Query");
        dialog.setHeaderText("Customer: " + selected.getCustomerName() + "\nQuestion: " + selected.getQueryText());

        ButtonType respondButton = new ButtonType("Send Response", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(respondButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextArea responseArea = new TextArea();
        responseArea.setPromptText("Type your response here...");
        responseArea.setPrefRowCount(5);
        responseArea.setPrefWidth(450);

        if (selected.getResponseText() != null && !selected.getResponseText().isEmpty()) {
            responseArea.setText(selected.getResponseText());
        }

        grid.add(new Label("Response:"), 0, 0);
        grid.add(responseArea, 0, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == respondButton) {
                return responseArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(response -> {
            if (response != null && !response.trim().isEmpty()) {
                try {
                    String sql = "{call respond_to_query(?, ?)}";
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    java.sql.CallableStatement cstmt = conn.prepareCall(sql);
                    cstmt.setInt(1, selected.getQueryId());
                    cstmt.setString(2, response);
                    cstmt.execute();
                    cstmt.close();
                    conn.close();

                    loadQueries();
                    showSuccessAlert("Response sent to customer!");
                } catch (SQLException e) {
                    showAlert("Error", "Failed to send response: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        loadVehicles();
        loadQueries();
        loadDashboardData();
        showSuccessAlert("Data refreshed!");
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

    private void showSuccessAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}