package com.vehicle.vehicleidentificationsystem.controller;

import com.vehicle.vehicleidentificationsystem.dao.DatabaseConnection;
import com.vehicle.vehicleidentificationsystem.model.User;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button clearButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loginProgress;
    @FXML private VBox loginCard;
    @FXML private VBox usernameFieldContainer;
    @FXML private VBox passwordFieldContainer;
    @FXML private HBox buttonContainer;  

    private DatabaseConnection dbConnection;

    @FXML
    public void initialize() {
        dbConnection = DatabaseConnection.getInstance();

        // Fade in animation for login card
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), loginCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Slide up animations for form fields
        TranslateTransition slide1 = new TranslateTransition(Duration.seconds(0.5), usernameFieldContainer);
        slide1.setFromY(30);
        slide1.setToY(0);
        slide1.play();

        FadeTransition fade1 = new FadeTransition(Duration.seconds(0.5), usernameFieldContainer);
        fade1.setFromValue(0);
        fade1.setToValue(1);
        fade1.play();

        TranslateTransition slide2 = new TranslateTransition(Duration.seconds(0.6), passwordFieldContainer);
        slide2.setFromY(30);
        slide2.setToY(0);
        slide2.play();

        FadeTransition fade2 = new FadeTransition(Duration.seconds(0.6), passwordFieldContainer);
        fade2.setFromValue(0);
        fade2.setToValue(1);
        fade2.play();

        TranslateTransition slide3 = new TranslateTransition(Duration.seconds(0.7), buttonContainer);
        slide3.setFromY(30);
        slide3.setToY(0);
        slide3.play();

        FadeTransition fade3 = new FadeTransition(Duration.seconds(0.7), buttonContainer);
        fade3.setFromValue(0);
        fade3.setToValue(1);
        fade3.play();

        // Button hover effects
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(15);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(javafx.scene.paint.Color.web("#764ba2", 0.5));
        loginButton.setEffect(dropShadow);

        loginButton.setOnMouseEntered(e -> {
            Glow glow = new Glow(0.5);
            loginButton.setEffect(glow);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), loginButton);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        loginButton.setOnMouseExited(e -> {
            loginButton.setEffect(dropShadow);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), loginButton);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // FadeTransition on login button
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), loginButton);
        fadeTransition.setFromValue(0.7);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();

        // Enter key handler
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) handleLogin();
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) handleLogin();
        });

        // Live validation on typing
        usernameField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                usernameField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-background-radius: 8;");
            } else {
                usernameField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-background-radius: 8;");
            }
        });

        passwordField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                passwordField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-background-radius: 8;");
            } else {
                passwordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-background-radius: 8;");
            }
        });
    }

    private void shakeAnimation() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), loginCard);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty()) {
            statusLabel.setText("Username is required");
            usernameField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            shakeAnimation();
            return;
        }
        if (password.isEmpty()) {
            statusLabel.setText("Password is required");
            passwordField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            shakeAnimation();
            return;
        }

        loginProgress.setVisible(true);
        loginButton.setDisable(true);
        statusLabel.setText("Authenticating...");

        RotateTransition rt = new RotateTransition(Duration.seconds(1), loginProgress);
        rt.setByAngle(360);
        rt.setCycleCount(RotateTransition.INDEFINITE);
        rt.play();

        new Thread(() -> {
            try {
                LoginResult result = authenticateUser(username, password);

                Platform.runLater(() -> {
                    loginProgress.setVisible(false);
                    loginButton.setDisable(false);
                    rt.stop();

                    if (result.success) {
                        statusLabel.setStyle("-fx-text-fill: #27ae60;");
                        statusLabel.setText("Login successful! Redirecting...");
                        loadDashboard(result.user);
                    } else {
                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                        if (result.userExists && result.accountDisabled) {
                            statusLabel.setText("Account is disabled. Please contact administrator.");
                        } else if (!result.userExists) {
                            statusLabel.setText("Invalid username or password");
                        } else {
                            statusLabel.setText("Invalid username or password");
                        }
                        shakeAnimation();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loginProgress.setVisible(false);
                    loginButton.setDisable(false);
                    statusLabel.setText("Database connection error");
                    shakeAnimation();
                });
                e.printStackTrace();
            }
        }).start();
    }

    private LoginResult authenticateUser(String username, String password) {
        LoginResult result = new LoginResult();
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    result.userExists = true;
                    boolean isActive = rs.getBoolean("is_active");
                    String dbPassword = rs.getString("password");

                    if (dbPassword.equals(password) && isActive) {
                        result.success = true;
                        result.accountDisabled = false;
                        User user = new User();
                        user.setUserId(rs.getInt("user_id"));
                        user.setUsername(rs.getString("username"));
                        user.setRole(rs.getString("role"));
                        user.setFullName(rs.getString("full_name"));
                        user.setEmail(rs.getString("email"));
                        user.setPhone(rs.getString("phone"));
                        user.setActive(rs.getBoolean("is_active"));
                        result.user = user;

                        String updateSql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, user.getUserId());
                            updateStmt.executeUpdate();
                        }
                    } else if (!isActive) {
                        result.success = false;
                        result.accountDisabled = true;
                    } else {
                        result.success = false;
                        result.accountDisabled = false;
                    }
                } else {
                    result.success = false;
                    result.userExists = false;
                    result.accountDisabled = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void loadDashboard(User user) {
        try {
            String fxmlFile;
            String title;

            switch (user.getRole()) {
                case "ADMIN":
                    fxmlFile = "admin_dashboard.fxml";
                    title = "Admin Dashboard";
                    break;
                case "CUSTOMER":
                    fxmlFile = "customer_dashboard.fxml";
                    title = "Customer Dashboard";
                    break;
                case "POLICE":
                    fxmlFile = "police_dashboard.fxml";
                    title = "Police Dashboard";
                    break;
                case "WORKSHOP":
                    fxmlFile = "workshop_dashboard.fxml";
                    title = "Workshop Dashboard";
                    break;
                case "INSURANCE":
                    fxmlFile = "insurance_dashboard.fxml";
                    title = "Insurance Dashboard";
                    break;
                default:
                    showErrorAlert("Unknown role: " + user.getRole());
                    return;
            }

            String resourcePath = "/com/vehicle/vehicleidentificationsystem/" + fxmlFile;
            java.net.URL location = getClass().getResource(resourcePath);

            if (location == null) {
                showErrorAlert("FXML file not found: " + resourcePath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(location);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AdminController) {
                ((AdminController) controller).setUser(user);
            } else if (controller instanceof CustomerController) {
                ((CustomerController) controller).setUser(user);
            } else if (controller instanceof PoliceController) {
                ((PoliceController) controller).setUser(user);
            } else if (controller instanceof WorkshopController) {
                ((WorkshopController) controller).setUser(user);
            } else if (controller instanceof InsuranceController) {
                ((InsuranceController) controller).setUser(user);
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Vehicle Identification System - " + title);
            stage.setScene(new Scene(root, 1400, 900));
            stage.setMinWidth(1200);
            stage.setMinHeight(700);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Could not load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        usernameField.clear();
        passwordField.clear();
        statusLabel.setText("");
        usernameField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
        passwordField.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class LoginResult {
        boolean success = false;
        boolean userExists = false;
        boolean accountDisabled = false;
        User user = null;
    }
}
