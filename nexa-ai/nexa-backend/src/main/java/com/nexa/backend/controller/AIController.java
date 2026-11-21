package com.nexa.backend.controller;

import com.nexa.backend.dto.request.CreateAIProviderRequest;
import com.nexa.backend.dto.response.AIProviderResponse;
import com.nexa.backend.service.AIService;
import com.nexa.common.dto.ApiResponse;
import com.nexa.security.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<AIProviderResponse>>> getProviders(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<AIProviderResponse> providers = aiService.getProviders(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @PostMapping("/providers")
    public ResponseEntity<ApiResponse<AIProviderResponse>> createProvider(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAIProviderRequest request) {
        AIProviderResponse provider = aiService.createProvider(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(provider));
    }

    @PatchMapping("/providers/{id}")
    public ResponseEntity<ApiResponse<AIProviderResponse>> updateProvider(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody CreateAIProviderRequest request) {
        AIProviderResponse provider = aiService.updateProvider(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(provider));
    }

    @DeleteMapping("/providers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProvider(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        aiService.deleteProvider(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/providers/{id}/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConnection(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        Map<String, Object> result = aiService.testConnection(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/providers/{id}/models")
    public ResponseEntity<ApiResponse<List<String>>> getModels(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        List<String> models = aiService.listModels(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(models));
    }
}
