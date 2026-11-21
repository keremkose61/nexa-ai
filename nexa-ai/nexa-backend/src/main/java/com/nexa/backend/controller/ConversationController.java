package com.nexa.backend.controller;

import com.nexa.backend.dto.request.CreateConversationRequest;
import com.nexa.backend.dto.request.UpdateConversationRequest;
import com.nexa.backend.dto.response.ConversationResponse;
import com.nexa.backend.service.ConversationService;
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
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> getConversations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ConversationResponse> response;
        if (query != null && !query.trim().isEmpty()) {
            response = conversationService.searchConversations(principal.getId(), query.trim(), pageable);
        } else {
            response = conversationService.getConversations(principal.getId(), pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateConversationRequest request) {
        ConversationResponse response = conversationService.createConversation(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        ConversationResponse response = conversationService.getConversation(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> updateConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody UpdateConversationRequest request) {
        ConversationResponse response = conversationService.updateConversation(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        conversationService.deleteConversation(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted", null));
    }
}
