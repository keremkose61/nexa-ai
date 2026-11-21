package com.nexa.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIProviderResponse {
    private UUID id;
    private String name;
    private String providerType;
    private String baseUrl;
    private String apiKeyMasked;
    private String defaultModel;
    private Double defaultTemperature;
    private Integer defaultMaxTokens;
    private boolean enabled;
    private boolean defaultProvider;
    private LocalDateTime lastTestedAt;
    private Boolean lastTestSuccess;
    private LocalDateTime createdAt;
}
