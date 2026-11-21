package com.nexa.ai.provider;

import com.nexa.ai.exception.AIProviderException;

import java.util.List;
import java.util.function.Consumer;

public interface AIProvider {
    String getProviderName();
    ProviderType getProviderType();
    AIResponse chat(AIRequest request) throws AIProviderException;
    void streamChat(AIRequest request, Consumer<String> tokenConsumer, Runnable onComplete, Consumer<Throwable> onError);
    boolean testConnection();
    List<String> listModels();
    void configure(ProviderConfig config);
    ProviderConfig getConfig();
}
