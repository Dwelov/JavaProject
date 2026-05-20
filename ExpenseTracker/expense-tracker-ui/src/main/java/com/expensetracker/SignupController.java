package com.expensetracker;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * SignupController — handles new account registration logic and navigation.
 */
public class SignupController implements Initializable {

    // ── FXML Bindings ──────────────────────────────────────────────────────────
    @FXML private TextField     fullNameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox      termsCheck;
    @FXML private Button        signupButton;
    @FXML private Label         errorLabel;
    @FXML private Region        errorSpacer;

    // ── Initialise ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tab navigation between fields
        fullNameField.setOnAction(e -> emailField.requestFocus());
        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> confirmPasswordField.requestFocus());
        confirmPasswordField.setOnAction(e -> handleSignup());

        hideError();
    }

    // ── Primary Action ─────────────────────────────────────────────────────────
    @FXML
    private void handleSignup() {
        String fullName  = fullNameField.getText().trim();
        String email     = emailField.getText().trim();
        String password  = passwordField.getText();
        String confirm   = confirmPasswordField.getText();

        // ── Validation chain ──
        if (fullName.isEmpty()) {
            showError("Please enter your full name.");
            shakeField(fullNameField);
            return;
        }

        if (fullName.length() < 2) {
            showError("Name must be at least 2 characters.");
            shakeField(fullNameField);
            return;
        }

        if (email.isEmpty()) {
            showError("Please enter your email address.");
            shakeField(emailField);
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            shakeField(emailField);
            return;
        }

        if (password.isEmpty()) {
            showError("Please choose a password.");
            shakeField(passwordField);
            return;
        }

        String passwordError = getPasswordError(password);
        if (passwordError != null) {
            showError(passwordError);
            shakeField(passwordField);
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match. Please try again.");
            shakeField(confirmPasswordField);
            confirmPasswordField.clear();
            return;
        }

        if (!termsCheck.isSelected()) {
            showError("You must agree to the Terms of Service to continue.");
            shakeField(termsCheck);
            return;
        }

        // Disable button while processing
        signupButton.setDisable(true);
        signupButton.setText("Creating account…");

        // Backend signup in background thread
        new Thread(() -> {
            String error = registerUser(fullName, email, password);

            javafx.application.Platform.runLater(() -> {
                if (error == null) {
                    showSuccess("Account created! Redirecting to login…");

                    // Brief pause then navigate
                    javafx.animation.PauseTransition pause =
                            new javafx.animation.PauseTransition(Duration.seconds(1.4));
                    pause.setOnFinished(e -> loadScene("login"));
                    pause.play();
                } else {
                    showError(error);
                    
                    // Intelligent field shaking
                    String lowerError = error.toLowerCase();
                    if (lowerError.contains("email")) {
                        shakeField(emailField);
                    } else if (lowerError.contains("password")) {
                        shakeField(passwordField);
                    } else if (lowerError.contains("name")) {
                        shakeField(fullNameField);
                    } else {
                        shakeField(signupButton);
                    }

                    signupButton.setDisable(false);
                    signupButton.setText("Create Account →");
                }
            });
        }).start();
    }

    @FXML
    private void handleGoToLogin() {
        loadScene("login");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
	private String registerUser(String fullName, String email, String password) {
        try {
            java.util.Map<String, String> request = java.util.Map.of(
                "fullName", fullName,
                "email", email,
                "password", password
            );
            java.util.Map<String, Object> response = ApiClient.post("/auth/signup", request, java.util.Map.class);
            if (response != null && response.containsKey("token")) {
                return null; // success
            }
            // Unexpected successful response shape
            return "Unexpected server response.";
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            System.err.println("Registration error: " + errorMsg);
            return (errorMsg != null && !errorMsg.isEmpty()) ? errorMsg : "Failed to create account. Please try again.";
        }
    }

    private void loadScene(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.getStyleClass().removeAll("success-label");
        if (!errorLabel.getStyleClass().contains("error-label")) {
            errorLabel.getStyleClass().add("error-label");
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        errorSpacer.setPrefHeight(12);

        FadeTransition ft = new FadeTransition(Duration.millis(200), errorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void showSuccess(String message) {
        errorLabel.getStyleClass().removeAll("error-label");
        if (!errorLabel.getStyleClass().contains("success-label")) {
            errorLabel.getStyleClass().add("success-label");
        }
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        errorSpacer.setPrefHeight(12);

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

    /** Returns null if password is valid, or an error message if not. */
    private String getPasswordError(String password) {
        if (password.length() < 6)
            return "Password must be at least 6 characters.";
        return null;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    private void shakeField(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }
}