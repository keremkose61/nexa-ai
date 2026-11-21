package com.nexa.backend.service;

import com.nexa.backend.dto.request.UpdateSettingsRequest;
import com.nexa.backend.entity.User;
import com.nexa.backend.entity.UserSettings;
import com.nexa.backend.repository.UserRepository;
import com.nexa.backend.repository.UserSettingsRepository;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserSettings getSettings(UUID userId) {
        return userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NexaException(ErrorCode.USER_NOT_FOUND));
                    UserSettings newSettings = UserSettings.builder()
                            .user(user)
                            .build();
                    return userSettingsRepository.save(newSettings);
                });
    }

    @Transactional
    public UserSettings updateSettings(UUID userId, UpdateSettingsRequest request) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NexaException(ErrorCode.USER_NOT_FOUND));
                    return UserSettings.builder()
                            .user(user)
                            .build();
                });

        if (request.getTheme() != null) {
            settings.setTheme(request.getTheme());
        }
        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }
        if (request.getFontSize() != null) {
            settings.setFontSize(request.getFontSize());
        }
        if (request.getDefaultProviderId() != null) {
            settings.setDefaultProviderId(request.getDefaultProviderId());
        }
        if (request.getDefaultModel() != null) {
            settings.setDefaultModel(request.getDefaultModel());
        }
        if (request.getDefaultTemperature() != null) {
            settings.setDefaultTemperature(request.getDefaultTemperature());
        }
        if (request.getDefaultMaxTokens() != null) {
            settings.setDefaultMaxTokens(request.getDefaultMaxTokens());
        }
        if (request.getCustomInstructions() != null) {
            settings.setCustomInstructions(request.getCustomInstructions());
        }
        if (request.getResponseLanguage() != null) {
            settings.setResponseLanguage(request.getResponseLanguage());
        }
        if (request.getConversationRetentionDays() != null) {
            settings.setConversationRetentionDays(request.getConversationRetentionDays());
        }
        if (request.getNotificationsEnabled() != null) {
            settings.setNotificationsEnabled(request.getNotificationsEnabled());
        }
        if (request.getSidebarCollapsed() != null) {
            settings.setSidebarCollapsed(request.getSidebarCollapsed());
        }

        return userSettingsRepository.save(settings);
    }
}
