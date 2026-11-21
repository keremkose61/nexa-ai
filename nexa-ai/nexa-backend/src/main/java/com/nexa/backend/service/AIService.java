package com.nexa.backend.service;

import com.nexa.ai.context.ContextManager;
import com.nexa.ai.openai.OpenAIProvider;
import com.nexa.ai.provider.*;
import com.nexa.backend.dto.request.CreateAIProviderRequest;
import com.nexa.backend.dto.request.SendMessageRequest;
import com.nexa.backend.dto.response.AIProviderResponse;
import com.nexa.backend.dto.response.MessageResponse;
import com.nexa.backend.entity.AIProviderEntity;
import com.nexa.backend.entity.AIUsageLog;
import com.nexa.backend.entity.Conversation;
import com.nexa.backend.entity.User;
import com.nexa.backend.repository.AIProviderEntityRepository;
import com.nexa.backend.repository.AIUsageLogRepository;
import com.nexa.backend.repository.ConversationRepository;
import com.nexa.backend.repository.MessageRepository;
import com.nexa.backend.repository.UserRepository;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import com.nexa.security.crypto.ApiKeyEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final AIProviderEntityRepository providerRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AIUsageLogRepository usageLogRepository;
    private final ApiKeyEncryptor apiKeyEncryptor;
    private final ContextManager contextManager;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    @Qualifier("aiExecutor")
    private final Executor aiExecutor;

    @Transactional(readOnly = true)
    public List<AIProviderResponse> getProviders(UUID userId) {
        return providerRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public AIProviderResponse createProvider(UUID userId, CreateAIProviderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NexaException(ErrorCode.USER_NOT_FOUND));

        if (request.isDefaultProvider()) {
            clearOtherDefaults(userId);
        }

        String encryptedApiKey = request.getApiKey() != null
                ? apiKeyEncryptor.encrypt(request.getApiKey())
                : null;

        AIProviderEntity entity = AIProviderEntity.builder()
                .user(user)
                .name(request.getName())
                .providerType(request.getProviderType())
                .baseUrl(request.getBaseUrl())
                .apiKeyEncrypted(encryptedApiKey)
                .defaultModel(request.getDefaultModel())
                .defaultTemperature(request.getDefaultTemperature() != null ? request.getDefaultTemperature() : 0.7)
                .defaultMaxTokens(request.getDefaultMaxTokens() != null ? request.getDefaultMaxTokens() : 2048)
                .timeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 60)
                .enabled(request.isEnabled())
                .defaultProvider(request.isDefaultProvider())
                .build();

        entity = providerRepository.save(entity);
        log.info("Created AI provider {} for user {}", entity.getId(), userId);
        return mapToResponse(entity);
    }

    @Transactional
    public AIProviderResponse updateProvider(UUID userId, UUID id, CreateAIProviderRequest request) {
        AIProviderEntity entity = providerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.AI_PROVIDER_NOT_FOUND));

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getProviderType() != null) {
            entity.setProviderType(request.getProviderType());
        }
        if (request.getBaseUrl() != null) {
            entity.setBaseUrl(request.getBaseUrl());
        }
        if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            entity.setApiKeyEncrypted(apiKeyEncryptor.encrypt(request.getApiKey()));
        }
        if (request.getDefaultModel() != null) {
            entity.setDefaultModel(request.getDefaultModel());
        }
        if (request.getDefaultTemperature() != null) {
            entity.setDefaultTemperature(request.getDefaultTemperature());
        }
        if (request.getDefaultMaxTokens() != null) {
            entity.setDefaultMaxTokens(request.getDefaultMaxTokens());
        }
        if (request.getTimeoutSeconds() != null) {
            entity.setTimeoutSeconds(request.getTimeoutSeconds());
        }
        entity.setEnabled(request.isEnabled());

        if (request.isDefaultProvider() && !entity.isDefaultProvider()) {
            clearOtherDefaults(userId);
            entity.setDefaultProvider(true);
        } else if (!request.isDefaultProvider()) {
            entity.setDefaultProvider(false);
        }

        entity = providerRepository.save(entity);
        return mapToResponse(entity);
    }

    @Transactional
    public void deleteProvider(UUID userId, UUID id) {
        AIProviderEntity entity = providerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.AI_PROVIDER_NOT_FOUND));
        providerRepository.delete(entity);
        log.info("Deleted AI provider {} for user {}", id, userId);
    }

    @Transactional
    public Map<String, Object> testConnection(UUID userId, UUID id) {
        AIProviderEntity entity = providerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.AI_PROVIDER_NOT_FOUND));

        AIProvider provider = buildAIProvider(entity);
        boolean success = false;
        try {
            success = provider.testConnection();
        } catch (Exception e) {
            log.warn("Connection test failed for provider {}: {}", id, e.getMessage());
        }

        entity.setLastTestedAt(LocalDateTime.now());
        entity.setLastTestSuccess(success);
        providerRepository.save(entity);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("testedAt", entity.getLastTestedAt());
        result.put("providerName", entity.getName());
        return result;
    }

    @Transactional(readOnly = true)
    public List<String> listModels(UUID userId, UUID id) {
        AIProviderEntity entity = providerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.AI_PROVIDER_NOT_FOUND));

        AIProvider provider = buildAIProvider(entity);
        try {
            return provider.listModels();
        } catch (Exception e) {
            log.warn("Failed to list models for provider {}: {}", id, e.getMessage());
            if (entity.getDefaultModel() != null) {
                return List.of(entity.getDefaultModel());
            }
            return List.of("gpt-4o", "gpt-4o-mini");
        }
    }

    @Transactional
    public MessageResponse processChat(UUID userId, UUID conversationId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.CONVERSATION_NOT_FOUND));

        // 1. Save user message
        messageService.saveUserMessage(conversationId, userId, request.getContent());

        // 2. Select AI Provider
        AIProviderEntity providerEntity = getEffectiveProvider(userId, conversation.getAiProviderId());
        String model = request.getAiModel() != null ? request.getAiModel()
                : (conversation.getAiModel() != null ? conversation.getAiModel()
                : (providerEntity != null && providerEntity.getDefaultModel() != null ? providerEntity.getDefaultModel() : "gpt-4o"));

        double temperature = request.getTemperature() != null ? request.getTemperature()
                : (providerEntity != null && providerEntity.getDefaultTemperature() != null ? providerEntity.getDefaultTemperature() : 0.7);

        int maxTokens = request.getMaxTokens() != null ? request.getMaxTokens()
                : (providerEntity != null && providerEntity.getDefaultMaxTokens() != null ? providerEntity.getDefaultMaxTokens() : 2048);

        // 3. Build messages list
        List<AIMessage> messages = new ArrayList<>();
        var dbMessages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        for (var msg : dbMessages) {
            if (msg.getRole() == com.nexa.backend.entity.Message.MessageRole.USER) {
                messages.add(AIMessage.user(msg.getContent()));
            } else if (msg.getRole() == com.nexa.backend.entity.Message.MessageRole.ASSISTANT) {
                messages.add(AIMessage.assistant(msg.getContent()));
            } else if (msg.getRole() == com.nexa.backend.entity.Message.MessageRole.SYSTEM) {
                messages.add(AIMessage.system(msg.getContent()));
            }
        }

        AIRequest initialAiRequest = AIRequest.builder()
                .messages(messages)
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .stream(request.isStream())
                .build();

        final AIRequest aiRequest = (conversation.getSystemPrompt() != null && !conversation.getSystemPrompt().isBlank())
                ? initialAiRequest.withSystemPrompt(conversation.getSystemPrompt())
                : initialAiRequest;

        AIProvider provider = buildAIProvider(providerEntity);

        if (request.isStream()) {
            StringBuilder completeResponse = new StringBuilder();
            aiExecutor.execute(() -> {
                try {
                    provider.streamChat(aiRequest,
                            token -> {
                                completeResponse.append(token);
                                messagingTemplate.convertAndSend("/topic/conversations/" + conversationId,
                                        Map.of("type", "token", "content", token));
                            },
                            () -> {
                                MessageResponse saved = messageService.saveAssistantMessage(
                                        conversationId, completeResponse.toString(), model, 0, 0, 0);
                                logUsage(userId, conversationId, saved.getId(),
                                        providerEntity != null ? providerEntity.getName() : "OpenAI", model, 0, 0);
                                messagingTemplate.convertAndSend("/topic/conversations/" + conversationId,
                                        Map.of("type", "done", "message", saved));
                            },
                            error -> {
                                log.error("Stream chat error: {}", error.getMessage());
                                messagingTemplate.convertAndSend("/topic/conversations/" + conversationId,
                                        Map.of("type", "error", "error", error.getMessage()));
                            });
                } catch (Exception e) {
                    log.error("Failed to execute stream chat", e);
                }
            });

            return MessageResponse.builder()
                    .role(com.nexa.backend.entity.Message.MessageRole.ASSISTANT)
                    .content("")
                    .status(com.nexa.backend.entity.Message.MessageStatus.STREAMING)
                    .aiModel(model)
                    .createdAt(LocalDateTime.now())
                    .build();
        } else {
            AIResponse aiResponse = provider.chat(aiRequest);
            int promptTokens = aiResponse.getUsage() != null ? aiResponse.getUsage().getPromptTokens() : 0;
            int completionTokens = aiResponse.getUsage() != null ? aiResponse.getUsage().getCompletionTokens() : 0;
            int totalTokens = aiResponse.getUsage() != null ? aiResponse.getUsage().getTotalTokens() : promptTokens + completionTokens;

            MessageResponse assistantMessage = messageService.saveAssistantMessage(
                    conversationId, aiResponse.getContent(), model, promptTokens, completionTokens, totalTokens);

            logUsage(userId, conversationId, assistantMessage.getId(),
                    providerEntity != null ? providerEntity.getName() : "OpenAI", model, promptTokens, completionTokens);

            return assistantMessage;
        }
    }

    private void logUsage(UUID userId, UUID convId, UUID msgId, String providerName,
                          String model, int promptTokens, int completionTokens) {
        try {
            User user = userRepository.getReferenceById(userId);
            AIUsageLog logEntry = AIUsageLog.builder()
                    .user(user)
                    .conversationId(convId)
                    .messageId(msgId)
                    .providerName(providerName)
                    .modelName(model)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .build();
            usageLogRepository.save(logEntry);
        } catch (Exception e) {
            log.warn("Failed to log AI usage: {}", e.getMessage());
        }
    }

    private AIProviderEntity getEffectiveProvider(UUID userId, UUID providerId) {
        if (providerId != null) {
            return providerRepository.findByIdAndUserId(providerId, userId).orElse(null);
        }
        return providerRepository.findByUserIdAndDefaultProviderTrue(userId)
                .orElseGet(() -> providerRepository.findByUserIdAndEnabledTrue(userId).stream().findFirst().orElse(null));
    }

    private AIProvider buildAIProvider(AIProviderEntity entity) {
        if (entity == null) {
            ProviderConfig config = ProviderConfig.builder()
                    .name("OpenAI-Fallback")
                    .type(ProviderType.OPENAI)
                    .baseUrl("https://api.openai.com/v1")
                    .apiKey(System.getenv().getOrDefault("OPENAI_API_KEY", ""))
                    .defaultModel("gpt-4o")
                    .defaultTemperature(0.7)
                    .defaultMaxTokens(2048)
                    .timeoutSeconds(60)
                    .enabled(true)
                    .build();
            return new OpenAIProvider(config);
        }

        String decryptedKey = null;
        if (entity.getApiKeyEncrypted() != null && !entity.getApiKeyEncrypted().isBlank()) {
            try {
                decryptedKey = apiKeyEncryptor.decrypt(entity.getApiKeyEncrypted());
            } catch (Exception e) {
                log.warn("Could not decrypt API key for provider {}: {}", entity.getId(), e.getMessage());
            }
        }

        ProviderConfig config = ProviderConfig.builder()
                .id(entity.getId() != null ? entity.getId().toString() : null)
                .name(entity.getName())
                .type(ProviderType.valueOf(entity.getProviderType().toUpperCase()))
                .baseUrl(entity.getBaseUrl())
                .apiKey(decryptedKey)
                .defaultModel(entity.getDefaultModel())
                .defaultTemperature(entity.getDefaultTemperature() != null ? entity.getDefaultTemperature() : 0.7)
                .defaultMaxTokens(entity.getDefaultMaxTokens() != null ? entity.getDefaultMaxTokens() : 2048)
                .timeoutSeconds(entity.getTimeoutSeconds() != null ? entity.getTimeoutSeconds() : 60)
                .enabled(entity.isEnabled())
                .build();

        return new OpenAIProvider(config);
    }

    private void clearOtherDefaults(UUID userId) {
        List<AIProviderEntity> providers = providerRepository.findByUserId(userId);
        for (AIProviderEntity p : providers) {
            if (p.isDefaultProvider()) {
                p.setDefaultProvider(false);
                providerRepository.save(p);
            }
        }
    }

    private AIProviderResponse mapToResponse(AIProviderEntity entity) {
        String maskedKey = null;
        if (entity.getApiKeyEncrypted() != null && !entity.getApiKeyEncrypted().isBlank()) {
            try {
                String plain = apiKeyEncryptor.decrypt(entity.getApiKeyEncrypted());
                maskedKey = apiKeyEncryptor.mask(plain);
            } catch (Exception e) {
                maskedKey = "****";
            }
        }

        return AIProviderResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .providerType(entity.getProviderType())
                .baseUrl(entity.getBaseUrl())
                .apiKeyMasked(maskedKey)
                .defaultModel(entity.getDefaultModel())
                .defaultTemperature(entity.getDefaultTemperature())
                .defaultMaxTokens(entity.getDefaultMaxTokens())
                .enabled(entity.isEnabled())
                .defaultProvider(entity.isDefaultProvider())
                .lastTestedAt(entity.getLastTestedAt())
                .lastTestSuccess(entity.getLastTestSuccess())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
