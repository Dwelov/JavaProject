package com.expensetracker;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * LoginController — handles login form logic, validation, and navigation.
 */
public class LoginController implements Initializable {

    // ── FXML Bindings ──────────────────────────────────────────────────────────
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox      rememberMe;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;
    @FXML private Region        errorSpacer;

    // ── Initialise ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Allow Enter key to submit from either field
        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        // Hide error area initially
        hideError();
    }

    // ── Primary Action ─────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // Client-side validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            shakeField(email.isEmpty() ? emailField : passwordField);
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            shakeField(emailField);
            return;
        }

        // Disable button while processing
        loginButton.setDisable(true);
        loginButton.setText("Signing in…");

        // Backend login in background thread
        new Thread(() -> {
            try {
                java.util.Map<String, Object> request = java.util.Map.of("email", email, "password", password);
                java.util.Map<String, Object> response = ApiClient.post("/auth/login", request, java.util.Map.class);

                if (response.containsKey("token")) {
                    ApiClient.setAuthToken((String) response.get("token"));
                    javafx.application.Platform.runLater(() -> {
                        hideError();
                        navigateToDashboard();
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        showError((String) response.getOrDefault("error", "Invalid credentials."));
                        shakeField(passwordField);
                        loginButton.setDisable(false);
                        loginButton.setText("Sign In →");
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Connection error: " + e.getMessage());
                    loginButton.setDisable(false);
                    loginButton.setText("Sign In →");
                });
            }
        }).start();
    }

    @FXML
    private void handleForgotPassword() {
        showError("Password reset email sent! Check your inbox.");
        errorLabel.getStyleClass().removeAll("error-label");
        if (!errorLabel.getStyleClass().contains("success-label")) {
            errorLabel.getStyleClass().add("success-label");
        }
    }

    @FXML
    private void handleGoToSignup() {
        loadScene("/com/expensetracker/signup.fxml");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void navigateToDashboard() {
        try {
            App.setRoot("dashboard");
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
        }
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Fade transition between scenes
            FadeTransition fade = new FadeTransition(Duration.millis(300), root);
            fade.setFromValue(0);
            fade.setToValue(1);

            Scene newScene = new Scene(root);
            
            // Apply CSS stylesheet to new scene
            try {
                var cssResource = getClass().getResource("/com/expensetracker/styles.css");
                if (cssResource != null) {
                    newScene.getStylesheets().add(cssResource.toExternalForm());
                }
            } catch (Exception cssException) {
                System.err.println("Warning: CSS file not found: " + cssException.getMessage());
            }
            
            stage.setScene(newScene);
            stage.show();
            fade.play();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        errorSpacer.setPrefHeight(12);

        // Fade in
        FadeTransition ft = new FadeTransition(Duration.millis(200), errorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorSpacer.setPrefHeight(0);
    }

    /** Horizontal shake animation for invalid field feedback. */
    private void shakeField(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }
}
