package com.nexa.backend.dto.response;

import com.nexa.backend.entity.UploadedFile.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFileResponse {
    private UUID id;
    private UUID projectId;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private ProcessingStatus processingStatus;
    private String processingError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
