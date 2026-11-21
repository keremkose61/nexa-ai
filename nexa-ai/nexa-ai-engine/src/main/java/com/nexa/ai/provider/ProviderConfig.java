package com.nexa.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderConfig {
    private String id;
    private String name;
    private ProviderType type;
    private String baseUrl;
    private String apiKey;
    private String defaultModel;
    @Builder.Default
    private double defaultTemperature = 0.7;
    @Builder.Default
    private int defaultMaxTokens = 2048;
    @Builder.Default
    private int timeoutSeconds = 60;
    private boolean enabled;
}
