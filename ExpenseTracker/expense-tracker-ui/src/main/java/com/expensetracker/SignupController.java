package com.expensetracker;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * SignupController — handles new account registration logic and navigation.
 *
 * Wire-up:
 *   - Replace registerUser() stub with your UserService / UserRepository call.
 *   - Update loadScene() paths to match your project's FXML layout.
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

       
        boolean success = registerUser(fullName, email, password);

        if (success) {
            showSuccess("Account created! Redirecting to login…");

            // Brief pause then navigate
            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(Duration.seconds(1.4));
            pause.setOnFinished(e -> loadScene("/com/expensetracker/login.fxml"));
            pause.play();
        } else {
            showError("An account with this email already exists.");
            shakeField(emailField);
            signupButton.setDisable(false);
            signupButton.setText("Create Account →");
        }
    }

    @FXML
    private void handleGoToLogin() {
        loadScene("/com/expensetracker/login.fxml");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Stub — replace with your UserService.register() call. */
    private boolean registerUser(String fullName, String email, String password) {
       
        // Example: return userService.register(fullName, email, password);
        System.out.printf("Registering user: %s <%s>%n", fullName, email);
        return true;  // stub: always succeeds
    }

    private void loadScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) signupButton.getScene().getWindow();

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
        if (password.length() < 8)
            return "Password must be at least 8 characters.";
        if (!password.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter.";
        if (!password.matches(".*[0-9].*"))
            return "Password must contain at least one number.";
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