package com.nexa.desktop.app;

import com.nexa.desktop.navigation.AppScreen;
import com.nexa.desktop.navigation.Navigator;
import com.nexa.desktop.theme.ThemeManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexaApp extends Application {
    private static final Logger log = LoggerFactory.getLogger(NexaApp.class);

    @Override
    public void start(Stage primaryStage) {
        log.info("Starting NEXA AI Desktop Application...");

        // Initialize Theme Manager
        ThemeManager.getInstance().setTheme(ThemeManager.Theme.DARK);

        // Initialize Navigator with Primary Stage
        Navigator navigator = Navigator.getInstance();
        navigator.init(primaryStage);

        // Navigate to Login Screen initially
        navigator.navigateTo(AppScreen.LOGIN);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
