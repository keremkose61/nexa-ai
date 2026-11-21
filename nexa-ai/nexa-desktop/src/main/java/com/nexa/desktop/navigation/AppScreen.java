package com.nexa.desktop.navigation;

import lombok.Getter;

@Getter
public enum AppScreen {
    LOGIN("/fxml/Login.fxml", "title.login", false),
    MAIN("/fxml/Main.fxml", "title.main", false),
    DASHBOARD("/fxml/Dashboard.fxml", "nav.dashboard", true),
    CHAT("/fxml/Chat.fxml", "nav.chat", true),
    DOCUMENTS("/fxml/Documents.fxml", "nav.documents", true),
    CODE_ASSISTANT("/fxml/CodeAssistant.fxml", "nav.code_assistant", true),
    PROJECTS("/fxml/Projects.fxml", "nav.projects", true),
    FILES("/fxml/Files.fxml", "nav.files", true),
    ANALYTICS("/fxml/Analytics.fxml", "nav.analytics", true),
    SETTINGS("/fxml/Settings.fxml", "nav.settings", true),
    ADMIN("/fxml/Admin.fxml", "nav.admin", true);

    private final String fxmlPath;
    private final String titleKey;
    private final boolean nested;

    AppScreen(String fxmlPath, String titleKey, boolean nested) {
        this.fxmlPath = fxmlPath;
        this.titleKey = titleKey;
        this.nested = nested;
    }
}
