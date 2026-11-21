package com.nexa.desktop.controller;

import com.nexa.desktop.model.UserDto;
import com.nexa.desktop.navigation.AppScreen;
import com.nexa.desktop.navigation.Navigator;
import com.nexa.desktop.service.AuthApiService;
import com.nexa.desktop.theme.ThemeManager;
import com.nexa.desktop.util.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private Button btnDashboard;
    @FXML private Button btnChat;
    @FXML private Button btnDocuments;
    @FXML private Button btnCodeAssistant;
    @FXML private Button btnProjects;
    @FXML private Button btnFiles;
    @FXML private Button btnAnalytics;
    @FXML private Button btnSettings;
    @FXML private Button btnAdmin;

    @FXML private Label screenTitleLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Circle connectionStatusDot;
    @FXML private Label userDisplayNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Button themeToggleButton;
    @FXML private Button logoutButton;
    @FXML private StackPane contentPane;

    private final Map<AppScreen, Button> navButtons = new EnumMap<>(AppScreen.class);
    private final Map<AppScreen, Parent> screenCache = new EnumMap<>(AppScreen.class);
    private final AuthApiService authService = AuthApiService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Navigator.getInstance().registerMainController(this);

        // Map buttons to screens
        navButtons.put(AppScreen.DASHBOARD, btnDashboard);
        navButtons.put(AppScreen.CHAT, btnChat);
        navButtons.put(AppScreen.DOCUMENTS, btnDocuments);
        navButtons.put(AppScreen.CODE_ASSISTANT, btnCodeAssistant);
        navButtons.put(AppScreen.PROJECTS, btnProjects);
        navButtons.put(AppScreen.FILES, btnFiles);
        navButtons.put(AppScreen.ANALYTICS, btnAnalytics);
        navButtons.put(AppScreen.SETTINGS, btnSettings);
        navButtons.put(AppScreen.ADMIN, btnAdmin);

        // Load user info
        loadUserInfo();

        // Check backend status
        updateConnectionStatus(true);
    }

    private void loadUserInfo() {
        UserDto cached = authService.getCurrentUserCached();
        if (cached != null) {
            updateUserDisplay(cached);
        } else {
            authService.getCurrentUser()
                    .thenAcceptAsync(user -> FxUtils.runOnFxThread(() -> updateUserDisplay(user)))
                    .exceptionally(ex -> {
                        FxUtils.runOnFxThread(() -> {
                            if (userDisplayNameLabel != null) userDisplayNameLabel.setText("NEXA User");
                            if (userEmailLabel != null) userEmailLabel.setText("connected@nexa.ai");
                        });
                        return null;
                    });
        }
    }

    private void updateUserDisplay(UserDto user) {
        if (userDisplayNameLabel != null) {
            userDisplayNameLabel.setText(user.getDisplayName() != null ? user.getDisplayName() : "NEXA User");
        }
        if (userEmailLabel != null) {
            userEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "user@nexa.ai");
        }
        // Admin visibility
        if (btnAdmin != null) {
            boolean isAdmin = user.getRoles() != null &&
                    (user.getRoles().contains("ROLE_ADMIN") || user.getRoles().contains("ADMIN"));
            btnAdmin.setVisible(isAdmin);
            btnAdmin.setManaged(isAdmin);
        }
    }

    public void selectScreen(AppScreen screen) {
        if (!screen.isNested()) {
            return;
        }

        log.info("Switching main content to screen: {}", screen);

        // Update active class on nav buttons
        navButtons.forEach((appScreen, button) -> {
            if (button != null) {
                if (appScreen == screen) {
                    if (!button.getStyleClass().contains("sidebar-button-active")) {
                        button.getStyleClass().add("sidebar-button-active");
                    }
                } else {
                    button.getStyleClass().remove("sidebar-button-active");
                }
            }
        });

        // Update Title
        if (screenTitleLabel != null) {
            String title = formatScreenTitle(screen);
            screenTitleLabel.setText(title);
        }

        // Load view (cached or fresh)
        Parent view = screenCache.computeIfAbsent(screen, s -> Navigator.getInstance().loadNestedView(s));

        if (contentPane != null && view != null) {
            contentPane.getChildren().setAll(view);
        }
    }

    private String formatScreenTitle(AppScreen screen) {
        return switch (screen) {
            case DASHBOARD -> "Dashboard Overview";
            case CHAT -> "AI Conversation Workspace";
            case DOCUMENTS -> "Document Intelligence";
            case CODE_ASSISTANT -> "Code Generation & Debugger";
            case PROJECTS -> "AI Workspaces & Projects";
            case FILES -> "Knowledge Base Files";
            case ANALYTICS -> "Usage & Token Analytics";
            case SETTINGS -> "System & Model Settings";
            case ADMIN -> "Enterprise Administration";
            default -> "NEXA AI";
        };
    }

    public void updateConnectionStatus(boolean online) {
        if (connectionStatusLabel != null) {
            connectionStatusLabel.setText(online ? "API Connected" : "Offline Mode");
        }
        if (connectionStatusDot != null) {
            connectionStatusDot.setFill(javafx.scene.paint.Color.web(online ? "#22C7A9" : "#E24A4A"));
        }
    }

    // --- Sidebar Button Handlers ---

    @FXML public void onNavDashboard() { selectScreen(AppScreen.DASHBOARD); }
    @FXML public void onNavChat() { selectScreen(AppScreen.CHAT); }
    @FXML public void onNavDocuments() { selectScreen(AppScreen.DOCUMENTS); }
    @FXML public void onNavCodeAssistant() { selectScreen(AppScreen.CODE_ASSISTANT); }
    @FXML public void onNavProjects() { selectScreen(AppScreen.PROJECTS); }
    @FXML public void onNavFiles() { selectScreen(AppScreen.FILES); }
    @FXML public void onNavAnalytics() { selectScreen(AppScreen.ANALYTICS); }
    @FXML public void onNavSettings() { selectScreen(AppScreen.SETTINGS); }
    @FXML public void onNavAdmin() { selectScreen(AppScreen.ADMIN); }

    @FXML
    public void onToggleTheme() {
        ThemeManager.getInstance().toggleTheme();
    }

    @FXML
    public void onLogout() {
        if (FxUtils.showConfirm("Sign Out", "Are you sure you want to sign out of NEXA AI?")) {
            authService.logout();
            screenCache.clear();
            Navigator.getInstance().navigateTo(AppScreen.LOGIN);
        }
    }
}
