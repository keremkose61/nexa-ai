package com.nexa.desktop.controller;

import com.nexa.desktop.navigation.AppScreen;
import com.nexa.desktop.navigation.Navigator;
import com.nexa.desktop.service.AuthApiService;
import com.nexa.desktop.theme.ThemeManager;
import com.nexa.desktop.util.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LoginController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField displayNameField;
    @FXML private VBox displayNameBox;
    @FXML private Button submitButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Hyperlink toggleModeLink;
    @FXML private Hyperlink demoLoginLink;
    @FXML private ComboBox<String> languageSelector;
    @FXML private Button themeToggleButton;

    private boolean isRegisterMode = false;
    private final AuthApiService authService = AuthApiService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);

        if (displayNameBox != null) {
            displayNameBox.setVisible(false);
            displayNameBox.setManaged(false);
        }

        // Language selector
        if (languageSelector != null) {
            languageSelector.getItems().addAll("English (EN)", "Türkçe (TR)");
            languageSelector.getSelectionModel().select(0);
            languageSelector.setOnAction(e -> {
                String selected = languageSelector.getSelectionModel().getSelectedItem();
                if (selected != null && selected.contains("TR")) {
                    Navigator.getInstance().setLocale(Locale.of("tr"));
                } else {
                    Navigator.getInstance().setLocale(Locale.ENGLISH);
                }
            });
        }

        // Enter key submission
        if (passwordField != null) {
            passwordField.setOnAction(e -> handleSubmit());
        }
        if (emailField != null) {
            emailField.setOnAction(e -> {
                if (passwordField != null) passwordField.requestFocus();
            });
        }
    }

    @FXML
    public void handleSubmit() {
        clearError();

        String email = emailField != null && emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField != null && passwordField.getText() != null ? passwordField.getText().trim() : "";
        String displayName = displayNameField != null && displayNameField.getText() != null ? displayNameField.getText().trim() : "";

        // Validation
        if (email.isEmpty()) {
            showError("Please enter your email address.");
            emailField.requestFocus();
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address.");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            passwordField.requestFocus();
            return;
        }

        if (isRegisterMode && displayName.isEmpty()) {
            showError("Please enter your display name.");
            if (displayNameField != null) displayNameField.requestFocus();
            return;
        }

        setLoading(true);

        if (isRegisterMode) {
            authService.register(email, password, displayName)
                    .thenAcceptAsync(response -> {
                        FxUtils.runOnFxThread(() -> {
                            setLoading(false);
                            log.info("Registration successful for: {}", email);
                            Navigator.getInstance().navigateTo(AppScreen.MAIN);
                        });
                    })
                    .exceptionally(ex -> {
                        FxUtils.runOnFxThread(() -> {
                            setLoading(false);
                            log.error("Registration failed", ex);
                            showError("Registration failed: " + ex.getCause().getMessage());
                        });
                        return null;
                    });
        } else {
            authService.login(email, password)
                    .thenAcceptAsync(response -> {
                        FxUtils.runOnFxThread(() -> {
                            setLoading(false);
                            log.info("Login successful for: {}", email);
                            Navigator.getInstance().navigateTo(AppScreen.MAIN);
                        });
                    })
                    .exceptionally(ex -> {
                        FxUtils.runOnFxThread(() -> {
                            setLoading(false);
                            log.error("Login failed", ex);
                            String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                            showError("Authentication failed: " + msg);
                        });
                        return null;
                    });
        }
    }

    @FXML
    public void handleDemoLogin() {
        if (emailField != null) emailField.setText("admin@nexa.ai");
        if (passwordField != null) passwordField.setText("Admin123!");
        handleSubmit();
    }

    @FXML
    public void toggleMode() {
        isRegisterMode = !isRegisterMode;
        clearError();

        if (isRegisterMode) {
            submitButton.setText("Create Account");
            toggleModeLink.setText("Already have an account? Sign In");
            if (displayNameBox != null) {
                displayNameBox.setVisible(true);
                displayNameBox.setManaged(true);
            }
        } else {
            submitButton.setText("Sign In to NEXA");
            toggleModeLink.setText("Don't have an account? Create one");
            if (displayNameBox != null) {
                displayNameBox.setVisible(false);
                displayNameBox.setManaged(false);
            }
        }
    }

    @FXML
    public void toggleTheme() {
        ThemeManager.getInstance().toggleTheme();
    }

    private void setLoading(boolean loading) {
        if (submitButton != null) submitButton.setDisable(loading);
        if (progressIndicator != null) {
            progressIndicator.setVisible(loading);
            progressIndicator.setManaged(loading);
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
}
