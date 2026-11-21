package com.nexa.ai.provider;

public enum ProviderType {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com"),
    GOOGLE("Google AI", "https://generativelanguage.googleapis.com"),
    LOCAL("Local Server", "http://localhost:11434/v1"),
    CUSTOM("Custom Provider", "");

    private final String displayName;
    private final String defaultBaseUrl;

    ProviderType(String displayName, String defaultBaseUrl) {
        this.displayName = displayName;
        this.defaultBaseUrl = defaultBaseUrl;
    }

    public String getDisplayName() { return displayName; }
    public String getDefaultBaseUrl() { return defaultBaseUrl; }
}
