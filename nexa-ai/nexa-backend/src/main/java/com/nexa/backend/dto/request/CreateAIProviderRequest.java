package com.nexa.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAIProviderRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String providerType;
    @NotBlank
    private String baseUrl;
    private String apiKey;
    private String defaultModel;
    private Double defaultTemperature;
    private Integer defaultMaxTokens;
    private Integer timeoutSeconds;
    private boolean enabled = true;
    private boolean defaultProvider = false;
}
