package com.nexa.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIProviderDto {
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
