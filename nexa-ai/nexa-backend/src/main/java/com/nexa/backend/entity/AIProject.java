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
@Table(name = "ai_projects", indexes = {
    @Index(name = "idx_project_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "conversations", "documents"})
@ToString(exclude = {"user", "conversations", "documents"})
public class AIProject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "system_instructions", columnDefinition = "TEXT")
    private String systemInstructions;

    @Column(name = "emoji", length = 10)
    @Builder.Default
    private String emoji = "🤖";

    @Column(name = "color", length = 20)
    @Builder.Default
    private String color = "#7167F8";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Column(name = "is_favorite")
    @Builder.Default
    private boolean favorite = false;

    @ManyToMany
    @JoinTable(
        name = "project_conversations",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "conversation_id")
    )
    @Builder.Default
    private List<Conversation> conversations = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UploadedFile> documents = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ProjectStatus {
        ACTIVE, ARCHIVED, DELETED
    }
}
