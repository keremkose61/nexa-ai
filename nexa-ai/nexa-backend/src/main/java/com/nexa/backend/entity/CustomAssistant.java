package com.nexa.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "custom_assistants", indexes = {
    @Index(name = "idx_assistant_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomAssistant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "emoji", length = 10)
    @Builder.Default
    private String emoji = "🤖";

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "is_public")
    @Builder.Default
    private boolean isPublic = false;

    @Column(name = "is_favorite")
    @Builder.Default
    private boolean favorite = false;

    @Column(name = "use_count")
    @Builder.Default
    private int useCount = 0;

    @Column(name = "default_model", length = 100)
    private String defaultModel;

    @Column(name = "default_temperature")
    @Builder.Default
    private Double defaultTemperature = 0.7;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
