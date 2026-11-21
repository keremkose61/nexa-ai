package com.nexa.desktop.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import com.nexa.desktop.theme.ThemeManager;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class FxUtils {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private FxUtils() {}

    public static void runOnFxThread(Runnable action) {
        if (action == null) return;
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    public static CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, EXECUTOR);
    }

    public static <T> CompletableFuture<T> runAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, EXECUTOR);
    }

    public static void showError(String title, String message) {
        runOnFxThread(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title != null ? title : "Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            styleAlert(alert);
            alert.showAndWait();
        });
    }

    public static void showInfo(String title, String message) {
        runOnFxThread(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title != null ? title : "Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            styleAlert(alert);
            alert.showAndWait();
        });
    }

    public static void showWarning(String title, String message) {
        runOnFxThread(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title != null ? title : "Warning");
            alert.setHeaderText(null);
            alert.setContentText(message);
            styleAlert(alert);
            alert.showAndWait();
        });
    }

    public static boolean showConfirm(String title, String message) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title != null ? title : "Confirm Action");
            alert.setHeaderText(null);
            alert.setContentText(message);
            styleAlert(alert);
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        } else {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(title != null ? title : "Confirm Action");
                alert.setHeaderText(null);
                alert.setContentText(message);
                styleAlert(alert);
                Optional<ButtonType> result = alert.showAndWait();
                future.complete(result.isPresent() && result.get() == ButtonType.OK);
            });
            try {
                return future.get();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    private static void styleAlert(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        if (pane != null && pane.getScene() != null) {
            ThemeManager.getInstance().applyTheme(pane.getScene());
        }
    }
}
