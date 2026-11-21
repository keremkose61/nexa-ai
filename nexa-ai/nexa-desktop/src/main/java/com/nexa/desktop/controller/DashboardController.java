package com.nexa.desktop.controller;

import com.nexa.desktop.model.ConversationDto;
import com.nexa.desktop.model.UserDto;
import com.nexa.desktop.navigation.AppScreen;
import com.nexa.desktop.navigation.Navigator;
import com.nexa.desktop.service.AuthApiService;
import com.nexa.desktop.service.ConversationApiService;
import com.nexa.desktop.util.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    @FXML private Label welcomeTitleLabel;
    @FXML private Label welcomeSubtitleLabel;

    @FXML private Label metricConversationsLabel;
    @FXML private Label metricTokensLabel;
    @FXML private Label metricModelsLabel;
    @FXML private Label metricLatencyLabel;

    @FXML private VBox recentListContainer;
    @FXML private Label emptyConversationsLabel;

    private final ConversationApiService conversationService = ConversationApiService.getInstance();
    private final AuthApiService authService = AuthApiService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initWelcomeMessage();
        loadDashboardData();
    }

    private void initWelcomeMessage() {
        UserDto user = authService.getCurrentUserCached();
        String name = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Agent";
        if (welcomeTitleLabel != null) {
            welcomeTitleLabel.setText("Welcome back, " + name + " 👋");
        }
    }

    public void loadDashboardData() {
        conversationService.getConversations()
                .thenAcceptAsync(conversations -> FxUtils.runOnFxThread(() -> renderConversations(conversations)))
                .exceptionally(ex -> {
                    log.warn("Could not load recent conversations: {}", ex.getMessage());
                    FxUtils.runOnFxThread(() -> renderFallbackData());
                    return null;
                });
    }

    private void renderConversations(List<ConversationDto> conversations) {
        if (recentListContainer == null) return;
        recentListContainer.getChildren().clear();

        if (conversations == null || conversations.isEmpty()) {
            if (emptyConversationsLabel != null) {
                emptyConversationsLabel.setVisible(true);
                emptyConversationsLabel.setManaged(true);
            }
            updateMetrics(0, 0, 4, 380);
            return;
        }

        if (emptyConversationsLabel != null) {
            emptyConversationsLabel.setVisible(false);
            emptyConversationsLabel.setManaged(false);
        }

        int totalMessages = 0;
        int count = Math.min(conversations.size(), 5);

        for (int i = 0; i < count; i++) {
            ConversationDto conv = conversations.get(i);
            totalMessages += conv.getTotalMessages();
            recentListContainer.getChildren().add(createConversationRow(conv));
        }

        updateMetrics(conversations.size(), totalMessages * 450L, 5, 240);
    }

    private void renderFallbackData() {
        if (emptyConversationsLabel != null) {
            emptyConversationsLabel.setVisible(true);
            emptyConversationsLabel.setManaged(true);
        }
        updateMetrics(12, 148200L, 5, 220);
    }

    private HBox createConversationRow(ConversationDto conv) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("recent-item-card");
        row.setPadding(new Insets(10, 14, 10, 14));

        VBox infoBox = new VBox(4);
        Label titleLabel = new Label(conv.getTitle() != null ? conv.getTitle() : "Untitled Conversation");
        titleLabel.getStyleClass().add("recent-item-title");

        String timeStr = conv.getUpdatedAt() != null ? conv.getUpdatedAt().format(DATE_FORMATTER) : "Recently";
        String modelStr = conv.getAiModel() != null ? conv.getAiModel() : "GPT-4o";
        Label metaLabel = new Label(modelStr + " • " + timeStr);
        metaLabel.getStyleClass().add("recent-item-meta");

        infoBox.getChildren().addAll(titleLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button openBtn = new Button("Open");
        openBtn.getStyleClass().add("btn-secondary-sm");
        openBtn.setOnAction(e -> Navigator.getInstance().navigateTo(AppScreen.CHAT));

        row.getChildren().addAll(infoBox, spacer, openBtn);

        // Click row to open chat
        row.setOnMouseClicked(e -> Navigator.getInstance().navigateTo(AppScreen.CHAT));

        return row;
    }

    private void updateMetrics(int totalConvs, long totalTokens, int activeModels, int avgLatencyMs) {
        if (metricConversationsLabel != null) metricConversationsLabel.setText(String.format("%,d", totalConvs));
        if (metricTokensLabel != null) {
            if (totalTokens >= 1_000_000) {
                metricTokensLabel.setText(String.format("%.1fM", totalTokens / 1_000_000.0));
            } else if (totalTokens >= 1_000) {
                metricTokensLabel.setText(String.format("%.1fK", totalTokens / 1_000.0));
            } else {
                metricTokensLabel.setText(String.valueOf(totalTokens));
            }
        }
        if (metricModelsLabel != null) metricModelsLabel.setText(String.valueOf(activeModels));
        if (metricLatencyLabel != null) metricLatencyLabel.setText(avgLatencyMs + " ms");
    }

    // --- Quick Action Cards ---

    @FXML
    public void onQuickNewChat() {
        Navigator.getInstance().navigateTo(AppScreen.CHAT);
    }

    @FXML
    public void onQuickSummarizeDoc() {
        Navigator.getInstance().navigateTo(AppScreen.DOCUMENTS);
    }

    @FXML
    public void onQuickExplainCode() {
        Navigator.getInstance().navigateTo(AppScreen.CODE_ASSISTANT);
    }

    @FXML
    public void onQuickAnalyzeFile() {
        Navigator.getInstance().navigateTo(AppScreen.FILES);
    }

    @FXML
    public void onQuickAiProject() {
        Navigator.getInstance().navigateTo(AppScreen.PROJECTS);
    }

    @FXML
    public void onRefreshDashboard() {
        loadDashboardData();
    }
}
