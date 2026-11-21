package com.nexa.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAIProviderRequestDto {
    private String name;
    private String providerType;
    private String baseUrl;
    private String apiKey;
    private String defaultModel;
    private Double defaultTemperature;
    private Integer defaultMaxTokens;
    private boolean enabled;
    private boolean defaultProvider;
}
