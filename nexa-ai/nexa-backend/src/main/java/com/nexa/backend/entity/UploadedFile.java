package com.nexa.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "uploaded_files", indexes = {
    @Index(name = "idx_file_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private AIProject project;

    @Column(name = "original_name", nullable = false, length = 500)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 500)
    private String storedName;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "content_type", length = 200)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", length = 20)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Column(name = "processing_error", length = 500)
    private String processingError;

    @Column(name = "is_favorite")
    @Builder.Default
    private boolean favorite = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ProcessingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
