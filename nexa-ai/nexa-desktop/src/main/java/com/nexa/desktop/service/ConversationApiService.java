package com.nexa.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nexa.desktop.model.ConversationDto;
import com.nexa.desktop.model.CreateConversationRequestDto;
import com.nexa.desktop.model.MessageDto;
import com.nexa.desktop.model.SendMessageRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ConversationApiService {
    private static final Logger log = LoggerFactory.getLogger(ConversationApiService.class);
    private static final ConversationApiService INSTANCE = new ConversationApiService();

    private final ApiClient apiClient = ApiClient.getInstance();

    private ConversationApiService() {}

    public static ConversationApiService getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<List<ConversationDto>> getConversations() {
        return apiClient.getAsync("/conversations", new TypeReference<List<ConversationDto>>() {});
    }

    public CompletableFuture<ConversationDto> createConversation(String title, String aiModel) {
        CreateConversationRequestDto request = CreateConversationRequestDto.builder()
                .title(title)
                .aiModel(aiModel)
                .build();
        return apiClient.postAsync("/conversations", request, ConversationDto.class);
    }

    public CompletableFuture<ConversationDto> getConversation(UUID id) {
        return apiClient.getAsync("/conversations/" + id, ConversationDto.class);
    }

    public CompletableFuture<List<MessageDto>> getMessages(UUID conversationId) {
        return apiClient.getAsync("/conversations/" + conversationId + "/messages", new TypeReference<List<MessageDto>>() {});
    }

    public CompletableFuture<MessageDto> sendMessage(UUID conversationId, String content, String aiModel) {
        SendMessageRequestDto request = SendMessageRequestDto.builder()
                .conversationId(conversationId)
                .content(content)
                .aiModel(aiModel)
                .build();
        return apiClient.postAsync("/conversations/" + conversationId + "/messages", request, MessageDto.class);
    }

    public void streamMessage(UUID conversationId, String content, String aiModel,
                              Consumer<String> onChunk, Runnable onComplete, Consumer<Throwable> onError) {
        SendMessageRequestDto request = SendMessageRequestDto.builder()
                .conversationId(conversationId)
                .content(content)
                .aiModel(aiModel)
                .build();
        apiClient.streamPost("/conversations/" + conversationId + "/messages/stream", request, onChunk, onComplete, onError);
    }

    public CompletableFuture<Void> deleteConversation(UUID id) {
        return apiClient.deleteAsync("/conversations/" + id, Void.class);
    }
}
