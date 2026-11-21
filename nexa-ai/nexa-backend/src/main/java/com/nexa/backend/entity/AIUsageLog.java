package com.nexa.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_usage_logs", indexes = {
    @Index(name = "idx_usage_user_id", columnList = "user_id"),
    @Index(name = "idx_usage_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIUsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "provider_name", length = 100)
    private String providerName;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "prompt_tokens")
    @Builder.Default
    private Integer promptTokens = 0;

    @Column(name = "completion_tokens")
    @Builder.Default
    private Integer completionTokens = 0;

    @Column(name = "total_tokens")
    @Builder.Default
    private Integer totalTokens = 0;

    @Column(name = "request_duration_ms")
    private Long requestDurationMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_type", length = 50)
    private FeatureType featureType;

    @Column(name = "success")
    @Builder.Default
    private boolean success = true;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum FeatureType {
        CHAT, DOCUMENT_ANALYSIS, CODE_ASSIST, FILE_ANALYSIS, PROJECT
    }
}
