package com.nexa.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "theme", length = 20)
    @Builder.Default
    private String theme = "dark";

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "tr";

    @Column(name = "font_size", length = 20)
    @Builder.Default
    private String fontSize = "medium";

    @Column(name = "default_provider_id")
    private UUID defaultProviderId;

    @Column(name = "default_model", length = 100)
    private String defaultModel;

    @Column(name = "default_temperature")
    @Builder.Default
    private Double defaultTemperature = 0.7;

    @Column(name = "default_max_tokens")
    @Builder.Default
    private Integer defaultMaxTokens = 2048;

    @Column(name = "custom_instructions", columnDefinition = "TEXT")
    private String customInstructions;

    @Column(name = "response_language", length = 50)
    @Builder.Default
    private String responseLanguage = "tr";

    @Column(name = "conversation_retention_days")
    @Builder.Default
    private Integer conversationRetentionDays = 90;

    @Column(name = "notifications_enabled")
    @Builder.Default
    private boolean notificationsEnabled = true;

    @Column(name = "sidebar_collapsed")
    @Builder.Default
    private boolean sidebarCollapsed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
