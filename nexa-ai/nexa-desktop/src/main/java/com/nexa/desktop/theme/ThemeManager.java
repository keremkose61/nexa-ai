package com.nexa.desktop.theme;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;

public class ThemeManager {
    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);
    private static final ThemeManager INSTANCE = new ThemeManager();

    public enum Theme {
        DARK("Dark", "/css/dark-theme.css"),
        LIGHT("Light", "/css/light-theme.css");

        private final String displayName;
        private final String cssPath;

        Theme(String displayName, String cssPath) {
            this.displayName = displayName;
            this.cssPath = cssPath;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getCssPath() {
            return cssPath;
        }
    }

    private static final String COMPONENTS_CSS = "/css/components.css";

    private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(Theme.DARK);
    private Scene registeredScene;

    private ThemeManager() {
        currentTheme.addListener((obs, oldTheme, newTheme) -> {
            if (registeredScene != null) {
                applyTheme(registeredScene);
            }
        });
    }

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public ObjectProperty<Theme> currentThemeProperty() {
        return currentTheme;
    }

    public Theme getCurrentTheme() {
        return currentTheme.get();
    }

    public void setTheme(Theme theme) {
        if (theme != null && theme != currentTheme.get()) {
            log.info("Switching theme to {}", theme.getDisplayName());
            currentTheme.set(theme);
        }
    }

    public void toggleTheme() {
        setTheme(currentTheme.get() == Theme.DARK ? Theme.LIGHT : Theme.DARK);
    }

    public void registerScene(Scene scene) {
        this.registeredScene = scene;
        applyTheme(scene);
    }

    public void applyTheme(Scene scene) {
        if (scene == null) {
            return;
        }

        scene.getStylesheets().clear();

        // 1. Base components CSS
        URL componentsUrl = getClass().getResource(COMPONENTS_CSS);
        if (componentsUrl != null) {
            scene.getStylesheets().add(componentsUrl.toExternalForm());
        } else {
            log.warn("Components stylesheet not found at: {}", COMPONENTS_CSS);
        }

        // 2. Active Theme CSS
        Theme theme = currentTheme.get();
        URL themeUrl = getClass().getResource(theme.getCssPath());
        if (themeUrl != null) {
            scene.getStylesheets().add(themeUrl.toExternalForm());
        } else {
            log.warn("Theme stylesheet not found at: {}", theme.getCssPath());
        }
    }
}
