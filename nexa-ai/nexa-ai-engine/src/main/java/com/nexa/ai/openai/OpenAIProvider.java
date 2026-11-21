package com.nexa.ai.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexa.ai.exception.AIProviderException;
import com.nexa.ai.provider.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * OpenAI-compatible provider implementation using OkHttp3.
 * Compatible with OpenAI API and any OpenAI-compatible endpoint (e.g. Ollama, LM Studio).
 */
public class OpenAIProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAIProvider.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String DATA_PREFIX = "data: ";
    private static final String DONE_MARKER = "[DONE]";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProviderConfig config;
    private OkHttpClient httpClient;

    public OpenAIProvider() {
    }

    public OpenAIProvider(ProviderConfig config) {
        configure(config);
    }

    // -------------------------------------------------------------------------
    // AIProvider interface
    // -------------------------------------------------------------------------

    @Override
    public String getProviderName() {
        return config != null ? config.getName() : "OpenAI";
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.OPENAI;
    }

    @Override
    public void configure(ProviderConfig config) {
        this.config = config;
        int timeout = config.getTimeoutSeconds() > 0 ? config.getTimeoutSeconds() : 60;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public ProviderConfig getConfig() {
        return config;
    }

    @Override
    public AIResponse chat(AIRequest request) throws AIProviderException {
        ensureConfigured();
        OpenAIRequest oaiReq = toOpenAIRequest(request, false);
        String url = buildChatUrl();

        Request httpRequest = buildHttpRequest(url, oaiReq);

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            return handleSyncResponse(response);
        } catch (AIProviderException e) {
            throw e;
        } catch (IOException e) {
            throw new AIProviderException(
                    "Connection failed: " + e.getMessage(),
                    AIProviderException.ErrorType.CONNECTION_FAILED, e);
        }
    }

    @Override
    public void streamChat(AIRequest request,
                           Consumer<String> tokenConsumer,
                           Runnable onComplete,
                           Consumer<Throwable> onError) {
        ensureConfigured();
        OpenAIRequest oaiReq = toOpenAIRequest(request, true);
        String url = buildChatUrl();
        Request httpRequest = buildHttpRequest(url, oaiReq);

        httpClient.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError.accept(new AIProviderException(
                        "Stream connection failed: " + e.getMessage(),
                        AIProviderException.ErrorType.STREAM_FAILED, e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    int code = response.code();
                    response.close();
                    onError.accept(mapHttpError("Streaming failed", code));
                    return;
                }
                ResponseBody body = response.body();
                if (body == null) {
                    onError.accept(new AIProviderException(
                            "Empty streaming response body",
                            AIProviderException.ErrorType.INVALID_RESPONSE));
                    return;
                }
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(DATA_PREFIX)) {
                            String data = line.substring(DATA_PREFIX.length()).trim();
                            if (DONE_MARKER.equals(data)) {
                                break;
                            }
                            if (data.isEmpty()) continue;
                            try {
                                OpenAIResponse chunk = objectMapper.readValue(data, OpenAIResponse.class);
                                if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                                    OpenAIResponse.Choice choice = chunk.getChoices().get(0);
                                    if (choice.getDelta() != null
                                            && choice.getDelta().getContent() != null) {
                                        tokenConsumer.accept(choice.getDelta().getContent());
                                    }
                                }
                            } catch (JsonProcessingException parseEx) {
                                log.warn("Failed to parse SSE chunk: {}", data);
                            }
                        }
                    }
                    onComplete.run();
                } catch (IOException e) {
                    onError.accept(new AIProviderException(
                            "Stream read error: " + e.getMessage(),
                            AIProviderException.ErrorType.STREAM_FAILED, e));
                }
            }
        });
    }

    @Override
    public boolean testConnection() {
        ensureConfigured();
        try {
            List<String> models = listModels();
            return !models.isEmpty();
        } catch (Exception e) {
            log.warn("Connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> listModels() {
        ensureConfigured();
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/models";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return Collections.emptyList();
            }
            String bodyStr = response.body().string();
            var node = objectMapper.readTree(bodyStr);
            var dataNode = node.get("data");
            if (dataNode == null || !dataNode.isArray()) return Collections.emptyList();
            List<String> ids = new ArrayList<>();
            for (var item : dataNode) {
                var idNode = item.get("id");
                if (idNode != null) ids.add(idNode.asText());
            }
            return ids;
        } catch (IOException e) {
            log.warn("Failed to list models: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void ensureConfigured() {
        if (config == null || httpClient == null) {
            throw new AIProviderException(
                    "Provider not configured. Call configure() first.",
                    AIProviderException.ErrorType.CONNECTION_FAILED);
        }
    }

    private String buildChatUrl() {
        return config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
    }

    private OpenAIRequest toOpenAIRequest(AIRequest request, boolean stream) {
        List<OpenAIRequest.MessageDto> dtos = request.getMessages().stream()
                .map(m -> OpenAIRequest.MessageDto.builder()
                        .role(m.getRoleString())
                        .content(m.getContent())
                        .build())
                .collect(Collectors.toList());

        String model = request.getModel() != null ? request.getModel()
                : (config.getDefaultModel() != null ? config.getDefaultModel() : "gpt-4o-mini");

        return OpenAIRequest.builder()
                .model(model)
                .messages(dtos)
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .stream(stream)
                .build();
    }

    private Request buildHttpRequest(String url, OpenAIRequest oaiReq) {
        String json;
        try {
            json = objectMapper.writeValueAsString(oaiReq);
        } catch (JsonProcessingException e) {
            throw new AIProviderException(
                    "Failed to serialize request: " + e.getMessage(),
                    AIProviderException.ErrorType.INVALID_RESPONSE, e);
        }
        RequestBody body = RequestBody.create(json, JSON);
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
    }

    private AIResponse handleSyncResponse(Response response) throws IOException {
        int code = response.code();
        String bodyStr = response.body() != null ? response.body().string() : "";

        if (code == 401) {
            throw new AIProviderException("Authentication failed — check your API key.",
                    AIProviderException.ErrorType.AUTHENTICATION_FAILED, code);
        }
        if (code == 429) {
            throw new AIProviderException("Rate limit reached. Please wait before retrying.",
                    AIProviderException.ErrorType.RATE_LIMITED, code);
        }
        if (code == 404) {
            throw new AIProviderException("Model not found (404).",
                    AIProviderException.ErrorType.MODEL_NOT_FOUND, code);
        }
        if (!response.isSuccessful()) {
            throw new AIProviderException("API request failed with HTTP " + code + ": " + bodyStr,
                    AIProviderException.ErrorType.INVALID_RESPONSE, code);
        }

        OpenAIResponse oaiResp;
        try {
            oaiResp = objectMapper.readValue(bodyStr, OpenAIResponse.class);
        } catch (JsonProcessingException e) {
            throw new AIProviderException("Failed to parse API response: " + e.getMessage(),
                    AIProviderException.ErrorType.INVALID_RESPONSE, e);
        }

        String content = "";
        String finishReason = null;
        if (oaiResp.getChoices() != null && !oaiResp.getChoices().isEmpty()) {
            OpenAIResponse.Choice choice = oaiResp.getChoices().get(0);
            if (choice.getMessage() != null) content = choice.getMessage().getContent();
            finishReason = choice.getFinishReason();
        }

        AIUsage usage = null;
        if (oaiResp.getUsage() != null) {
            usage = AIUsage.builder()
                    .promptTokens(oaiResp.getUsage().getPromptTokens())
                    .completionTokens(oaiResp.getUsage().getCompletionTokens())
                    .totalTokens(oaiResp.getUsage().getTotalTokens())
                    .build();
        }

        return AIResponse.builder()
                .id(oaiResp.getId())
                .content(content)
                .model(oaiResp.getModel())
                .usage(usage)
                .finishReason(finishReason)
                .build();
    }

    private AIProviderException mapHttpError(String prefix, int code) {
        return switch (code) {
            case 401 -> new AIProviderException(prefix + ": Authentication failed (401).",
                    AIProviderException.ErrorType.AUTHENTICATION_FAILED, code);
            case 429 -> new AIProviderException(prefix + ": Rate limited (429).",
                    AIProviderException.ErrorType.RATE_LIMITED, code);
            case 404 -> new AIProviderException(prefix + ": Model not found (404).",
                    AIProviderException.ErrorType.MODEL_NOT_FOUND, code);
            default -> new AIProviderException(prefix + ": HTTP " + code,
                    AIProviderException.ErrorType.INVALID_RESPONSE, code);
        };
    }
}
