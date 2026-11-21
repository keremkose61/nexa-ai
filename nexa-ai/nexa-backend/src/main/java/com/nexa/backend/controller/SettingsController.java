package com.nexa.backend.controller;

import com.nexa.backend.dto.request.UpdateSettingsRequest;
import com.nexa.backend.entity.UserSettings;
import com.nexa.backend.service.UserSettingsService;
import com.nexa.common.dto.ApiResponse;
import com.nexa.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserSettingsService settingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserSettings>> getSettings(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserSettings settings = settingsService.getSettings(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserSettings>> updateSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateSettingsRequest request) {
        UserSettings settings = settingsService.updateSettings(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
}
