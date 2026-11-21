package com.nexa.desktop.navigation;

import com.nexa.desktop.controller.MainController;
import com.nexa.desktop.theme.ThemeManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Navigator {
    private static final Logger log = LoggerFactory.getLogger(Navigator.class);
    private static final Navigator INSTANCE = new Navigator();

    private Stage primaryStage;
    private Scene mainScene;
    private MainController mainController;
    private AppScreen currentScreen;
    private ResourceBundle resourceBundle;
    private Locale currentLocale = Locale.ENGLISH;

    private final Map<AppScreen, Parent> cachedViews = new EnumMap<>(AppScreen.class);

    private Navigator() {
        loadResourceBundle();
    }

    public static Navigator getInstance() {
        return INSTANCE;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("NEXA AI — Your Personal AI Intelligence Platform");
        this.primaryStage.setMinWidth(960);
        this.primaryStage.setMinHeight(600);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        loadResourceBundle();
        cachedViews.clear();
        if (currentScreen != null) {
            navigateTo(currentScreen);
        }
    }

    private void loadResourceBundle() {
        try {
            resourceBundle = ResourceBundle.getBundle("i18n.messages", currentLocale);
        } catch (Exception e) {
            log.warn("Could not load bundle for locale {}, falling back to default", currentLocale, e);
            resourceBundle = ResourceBundle.getBundle("i18n.messages", Locale.ROOT);
        }
    }

    public void registerMainController(MainController controller) {
        this.mainController = controller;
    }

    public void navigateTo(AppScreen screen) {
        log.info("Navigating to: {}", screen);
        this.currentScreen = screen;

        if (screen == AppScreen.LOGIN) {
            loadRootScreen(screen, 1024, 680, false);
            mainController = null;
            return;
        }

        if (screen == AppScreen.MAIN) {
            loadRootScreen(screen, 1280, 820, true);
            if (mainController != null) {
                mainController.selectScreen(AppScreen.DASHBOARD);
            }
            return;
        }

        // Nested screens
        if (screen.isNested()) {
            if (mainController == null) {
                loadRootScreen(AppScreen.MAIN, 1280, 820, true);
            }
            if (mainController != null) {
                mainController.selectScreen(screen);
            }
        }
    }

    private void loadRootScreen(AppScreen screen, double width, double height, boolean resizable) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screen.getFxmlPath()), resourceBundle);
            Parent root = loader.load();

            if (screen == AppScreen.MAIN) {
                mainController = loader.getController();
            }

            if (mainScene == null) {
                mainScene = new Scene(root, width, height);
                primaryStage.setScene(mainScene);
            } else {
                mainScene.setRoot(root);
                if (!primaryStage.isMaximized()) {
                    primaryStage.setWidth(width);
                    primaryStage.setHeight(height);
                }
            }

            primaryStage.setResizable(resizable);
            ThemeManager.getInstance().registerScene(mainScene);

            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }

            updateStageTitle(screen);
        } catch (IOException e) {
            log.error("Failed to load root screen: {}", screen.getFxmlPath(), e);
            throw new RuntimeException("Failed to load screen: " + screen, e);
        }
    }

    public Parent loadNestedView(AppScreen screen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screen.getFxmlPath()), resourceBundle);
            return loader.load();
        } catch (IOException e) {
            log.error("Failed to load nested view: {}", screen.getFxmlPath(), e);
            // Return placeholder if FXML is not found or fails
            return createFallbackView(screen);
        }
    }

    private Parent createFallbackView(AppScreen screen) {
        javafx.scene.layout.VBox placeholder = new javafx.scene.layout.VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.getStyleClass().add("card-pane");

        javafx.scene.control.Label title = new javafx.scene.control.Label(screen.name().replace('_', ' '));
        title.getStyleClass().add("section-title");

        javafx.scene.control.Label subtitle = new javafx.scene.control.Label("Module under active development in NEXA AI v1.0");
        subtitle.getStyleClass().add("text-muted");

        placeholder.getChildren().addAll(title, subtitle);
        return placeholder;
    }

    private void updateStageTitle(AppScreen screen) {
        String baseTitle = "NEXA AI — Your Personal AI Intelligence Platform";
        if (resourceBundle != null && resourceBundle.containsKey(screen.getTitleKey())) {
            primaryStage.setTitle(resourceBundle.getString(screen.getTitleKey()) + " | NEXA AI");
        } else {
            primaryStage.setTitle(baseTitle);
        }
    }

    public AppScreen getCurrentScreen() {
        return currentScreen;
    }
}
