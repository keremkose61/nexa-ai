package com.nexa.backend.service;

import com.nexa.backend.dto.request.CreateConversationRequest;
import com.nexa.backend.dto.request.UpdateConversationRequest;
import com.nexa.backend.dto.response.ConversationResponse;
import com.nexa.backend.entity.AIProject;
import com.nexa.backend.entity.Conversation;
import com.nexa.backend.entity.User;
import com.nexa.backend.repository.ConversationRepository;
import com.nexa.backend.repository.ProjectRepository;
import com.nexa.backend.repository.UserRepository;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public ConversationResponse createConversation(UUID userId, CreateConversationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NexaException(ErrorCode.USER_NOT_FOUND));

        String title = (request.getTitle() != null && !request.getTitle().trim().isEmpty())
                ? request.getTitle().trim()
                : "New Conversation";

        Conversation conversation = Conversation.builder()
                .user(user)
                .title(title)
                .aiProviderId(request.getAiProviderId())
                .aiModel(request.getAiModel() != null ? request.getAiModel() : "gpt-4o")
                .systemPrompt(request.getSystemPrompt())
                .status(Conversation.ConversationStatus.ACTIVE)
                .build();

        conversation = conversationRepository.save(conversation);

        if (request.getProjectId() != null) {
            AIProject project = projectRepository.findByIdAndUserId(request.getProjectId(), userId)
                    .orElse(null);
            if (project != null) {
                project.getConversations().add(conversation);
                projectRepository.save(project);
            }
        }

        log.info("Created conversation {} for user {}", conversation.getId(), userId);
        return mapToResponse(conversation);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> getConversations(UUID userId, Pageable pageable) {
        return conversationRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID userId, UUID conversationId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.CONVERSATION_NOT_FOUND));
        return mapToResponse(conversation);
    }

    @Transactional
    public ConversationResponse updateConversation(UUID userId, UUID id, UpdateConversationRequest request) {
        Conversation conversation = conversationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.CONVERSATION_NOT_FOUND));

        if (request.getTitle() != null) {
            conversation.setTitle(request.getTitle());
        }
        if (request.getFavorite() != null) {
            conversation.setFavorite(request.getFavorite());
        }
        if (request.getArchived() != null) {
            conversation.setArchived(request.getArchived());
        }
        if (request.getStatus() != null) {
            conversation.setStatus(request.getStatus());
        }
        if (request.getAiModel() != null) {
            conversation.setAiModel(request.getAiModel());
        }
        if (request.getAiProviderId() != null) {
            conversation.setAiProviderId(request.getAiProviderId());
        }
        if (request.getSystemPrompt() != null) {
            conversation.setSystemPrompt(request.getSystemPrompt());
        }

        conversation = conversationRepository.save(conversation);
        return mapToResponse(conversation);
    }

    @Transactional
    public void deleteConversation(UUID userId, UUID id) {
        Conversation conversation = conversationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.CONVERSATION_NOT_FOUND));
        conversationRepository.delete(conversation);
        log.info("Deleted conversation {} for user {}", id, userId);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> searchConversations(UUID userId, String query, Pageable pageable) {
        return conversationRepository.searchByTitle(userId, query, pageable)
                .map(this::mapToResponse);
    }

    public ConversationResponse mapToResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .status(conversation.getStatus())
                .favorite(conversation.isFavorite())
                .archived(conversation.isArchived())
                .aiModel(conversation.getAiModel())
                .totalMessages(conversation.getTotalMessages())
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
