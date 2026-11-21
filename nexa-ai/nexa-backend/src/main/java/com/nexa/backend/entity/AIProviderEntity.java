package com.nexa.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_providers", indexes = {
    @Index(name = "idx_provider_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIProviderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "provider_type", nullable = false, length = 50)
    private String providerType;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(name = "api_key_encrypted", length = 1000)
    private String apiKeyEncrypted;

    @Column(name = "default_model", length = 100)
    private String defaultModel;

    @Column(name = "default_temperature")
    @Builder.Default
    private Double defaultTemperature = 0.7;

    @Column(name = "default_max_tokens")
    @Builder.Default
    private Integer defaultMaxTokens = 2048;

    @Column(name = "timeout_seconds")
    @Builder.Default
    private Integer timeoutSeconds = 60;

    @Column(name = "is_enabled")
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "is_default")
    @Builder.Default
    private boolean defaultProvider = false;

    @Column(name = "last_tested_at")
    private LocalDateTime lastTestedAt;

    @Column(name = "last_test_success")
    private Boolean lastTestSuccess;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
