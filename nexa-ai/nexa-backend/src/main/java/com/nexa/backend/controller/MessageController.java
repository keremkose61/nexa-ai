package com.nexa.backend.controller;

import com.nexa.backend.dto.request.SendMessageRequest;
import com.nexa.backend.dto.response.MessageResponse;
import com.nexa.backend.service.AIService;
import com.nexa.backend.service.MessageService;
import com.nexa.common.dto.ApiResponse;
import com.nexa.security.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final AIService aiService;

    @GetMapping("/api/v1/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID conversationId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<MessageResponse> messages = messageService.getMessages(conversationId, principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/api/v1/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = aiService.processChat(principal.getId(), conversationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Message processed", response));
    }

    @DeleteMapping("/api/v1/messages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        messageService.deleteMessage(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Message deleted", null));
    }
}
