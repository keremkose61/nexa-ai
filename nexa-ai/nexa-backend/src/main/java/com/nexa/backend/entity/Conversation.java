package com.nexa.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_conv_user_id", columnList = "user_id"),
    @Index(name = "idx_conv_updated_at", columnList = "updated_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "messages"})
@ToString(exclude = {"user", "messages"})
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 500)
    @Builder.Default
    private String title = "New Conversation";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "is_favorite")
    @Builder.Default
    private boolean favorite = false;

    @Column(name = "is_archived")
    @Builder.Default
    private boolean archived = false;

    @Column(name = "ai_provider_id")
    private UUID aiProviderId;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "total_messages")
    @Builder.Default
    private int totalMessages = 0;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ConversationStatus {
        ACTIVE, ARCHIVED, DELETED
    }
}
