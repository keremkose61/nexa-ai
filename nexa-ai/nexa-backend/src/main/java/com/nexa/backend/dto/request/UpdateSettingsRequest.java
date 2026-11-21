package com.nexa.backend.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateSettingsRequest {
    private String theme;
    private String language;
    private String fontSize;
    private UUID defaultProviderId;
    private String defaultModel;
    private Double defaultTemperature;
    private Integer defaultMaxTokens;
    private String customInstructions;
    private String responseLanguage;
    private Integer conversationRetentionDays;
    private Boolean notificationsEnabled;
    private Boolean sidebarCollapsed;
}
