package com.nexa.backend.controller;

import com.nexa.backend.dto.response.UserResponse;
import com.nexa.backend.entity.User;
import com.nexa.backend.repository.ConversationRepository;
import com.nexa.backend.repository.MessageRepository;
import com.nexa.backend.repository.UserRepository;
import com.nexa.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserResponse> users = userRepository.findAll(pageable).map(this::mapToUserResponse);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countActiveUsers());
        stats.put("totalConversations", conversationRepository.count());
        stats.put("totalMessages", messageRepository.count());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/system-health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("jvmFreeMemoryBytes", freeMemory);
        health.put("jvmTotalMemoryBytes", totalMemory);
        health.put("jvmMaxMemoryBytes", maxMemory);
        health.put("availableProcessors", runtime.availableProcessors());
        health.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());

        return ResponseEntity.ok(ApiResponse.success(health));
    }

    private UserResponse mapToUserResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .enabled(user.isEnabled())
                .roles(roles)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
