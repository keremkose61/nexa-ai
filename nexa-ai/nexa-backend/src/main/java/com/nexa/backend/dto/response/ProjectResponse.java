package com.nexa.backend.dto.response;

import com.nexa.backend.entity.AIProject.ProjectStatus;
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
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private String systemInstructions;
    private String emoji;
    private String color;
    private ProjectStatus status;
    private boolean favorite;
    private int conversationCount;
    private int documentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
