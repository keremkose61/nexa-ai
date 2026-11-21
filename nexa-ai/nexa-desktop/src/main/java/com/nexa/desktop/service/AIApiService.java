package com.nexa.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nexa.desktop.model.AIProviderDto;
import com.nexa.desktop.model.CreateAIProviderRequestDto;
import com.nexa.desktop.model.ModelDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AIApiService {
    private static final Logger log = LoggerFactory.getLogger(AIApiService.class);
    private static final AIApiService INSTANCE = new AIApiService();

    private final ApiClient apiClient = ApiClient.getInstance();

    private AIApiService() {}

    public static AIApiService getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<List<AIProviderDto>> getProviders() {
        return apiClient.getAsync("/ai/providers", new TypeReference<List<AIProviderDto>>() {})
                .exceptionally(ex -> {
                    log.warn("Failed to fetch AI providers from backend, returning defaults: {}", ex.getMessage());
                    return getDefaultProviders();
                });
    }

    public CompletableFuture<AIProviderDto> createProvider(CreateAIProviderRequestDto request) {
        return apiClient.postAsync("/ai/providers", request, AIProviderDto.class);
    }

    public CompletableFuture<Boolean> testConnection(UUID providerId) {
        return apiClient.postAsync("/ai/providers/" + providerId + "/test", null, Boolean.class)
                .exceptionally(ex -> {
                    log.warn("Test connection failed for provider {}: {}", providerId, ex.getMessage());
                    return false;
                });
    }

    public CompletableFuture<List<ModelDto>> listModels() {
        return apiClient.getAsync("/ai/models", new TypeReference<List<ModelDto>>() {})
                .exceptionally(ex -> {
                    log.debug("Falling back to standard AI models catalog: {}", ex.getMessage());
                    return getDefaultModels();
                });
    }

    public CompletableFuture<List<ModelDto>> listModels(UUID providerId) {
        return apiClient.getAsync("/ai/providers/" + providerId + "/models", new TypeReference<List<ModelDto>>() {})
                .exceptionally(ex -> {
                    log.debug("Falling back to standard models for provider {}: {}", providerId, ex.getMessage());
                    return getDefaultModels();
                });
    }

    public List<ModelDto> getDefaultModels() {
        List<ModelDto> models = new ArrayList<>();
        models.add(ModelDto.builder()
                .id("gpt-4o")
                .name("GPT-4o (Omni)")
                .provider("OpenAI")
                .description("Flagship multimodal model with high intelligence and fast response")
                .contextWindow(128000)
                .vision(true)
                .reasoning(false)
                .build());
        models.add(ModelDto.builder()
                .id("claude-3-5-sonnet")
                .name("Claude 3.5 Sonnet")
                .provider("Anthropic")
                .description("Exceptional reasoning, code generation, and complex analysis")
                .contextWindow(200000)
                .vision(true)
                .reasoning(true)
                .build());
        models.add(ModelDto.builder()
                .id("gemini-1.5-pro")
                .name("Gemini 1.5 Pro")
                .provider("Google")
                .description("Massive context window with state-of-the-art audio, video and document analysis")
                .contextWindow(2000000)
                .vision(true)
                .reasoning(true)
                .build());
        models.add(ModelDto.builder()
                .id("llama-3.1-70b")
                .name("Llama 3.1 70B")
                .provider("Meta / Ollama")
                .description("Open-weights powerful local model for private processing")
                .contextWindow(128000)
                .vision(false)
                .reasoning(false)
                .build());
        models.add(ModelDto.builder()
                .id("deepseek-coder-v2")
                .name("DeepSeek Coder V2")
                .provider("DeepSeek")
                .description("Specialized code intelligence and multi-language development")
                .contextWindow(128000)
                .vision(false)
                .reasoning(true)
                .build());
        return models;
    }

    private List<AIProviderDto> getDefaultProviders() {
        List<AIProviderDto> providers = new ArrayList<>();
        providers.add(AIProviderDto.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .name("OpenAI Official")
                .providerType("OPENAI")
                .baseUrl("https://api.openai.com/v1")
                .apiKeyMasked("sk-proj-...8a1F")
                .defaultModel("gpt-4o")
                .defaultTemperature(0.7)
                .defaultMaxTokens(4096)
                .enabled(true)
                .defaultProvider(true)
                .lastTestSuccess(true)
                .build());
        providers.add(AIProviderDto.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                .name("Anthropic Claude")
                .providerType("ANTHROPIC")
                .baseUrl("https://api.anthropic.com/v1")
                .apiKeyMasked("sk-ant-...5k2E")
                .defaultModel("claude-3-5-sonnet")
                .defaultTemperature(0.5)
                .defaultMaxTokens(8192)
                .enabled(true)
                .defaultProvider(false)
                .lastTestSuccess(true)
                .build());
        return providers;
    }
}
