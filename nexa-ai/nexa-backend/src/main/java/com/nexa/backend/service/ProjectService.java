package com.nexa.backend.service;

import com.nexa.backend.dto.request.CreateProjectRequest;
import com.nexa.backend.dto.response.ProjectResponse;
import com.nexa.backend.entity.AIProject;
import com.nexa.backend.entity.User;
import com.nexa.backend.repository.ProjectRepository;
import com.nexa.backend.repository.UserRepository;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(UUID userId, CreateProjectRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NexaException(ErrorCode.USER_NOT_FOUND));

        AIProject project = AIProject.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .systemInstructions(request.getSystemInstructions())
                .emoji(request.getEmoji() != null ? request.getEmoji() : "🤖")
                .color(request.getColor() != null ? request.getColor() : "#7167F8")
                .status(AIProject.ProjectStatus.ACTIVE)
                .build();

        project = projectRepository.save(project);
        log.info("Created project {} for user {}", project.getId(), userId);
        return mapToResponse(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(UUID userId, Pageable pageable) {
        return projectRepository.findByUserIdAndStatusNotOrderByUpdatedAtDesc(
                userId, AIProject.ProjectStatus.DELETED, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID userId, UUID projectId) {
        AIProject project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.PROJECT_NOT_FOUND));
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID userId, UUID projectId, CreateProjectRequest request) {
        AIProject project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.PROJECT_NOT_FOUND));

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getSystemInstructions() != null) {
            project.setSystemInstructions(request.getSystemInstructions());
        }
        if (request.getEmoji() != null) {
            project.setEmoji(request.getEmoji());
        }
        if (request.getColor() != null) {
            project.setColor(request.getColor());
        }

        project = projectRepository.save(project);
        return mapToResponse(project);
    }

    @Transactional
    public void deleteProject(UUID userId, UUID projectId) {
        AIProject project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.PROJECT_NOT_FOUND));
        project.setStatus(AIProject.ProjectStatus.DELETED);
        projectRepository.save(project);
        log.info("Deleted project {} for user {}", projectId, userId);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchProjects(UUID userId, String query, Pageable pageable) {
        return projectRepository.searchByName(userId, query, pageable)
                .map(this::mapToResponse);
    }

    public ProjectResponse mapToResponse(AIProject project) {
        int convCount = project.getConversations() != null ? project.getConversations().size() : 0;
        int docCount = project.getDocuments() != null ? project.getDocuments().size() : 0;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .systemInstructions(project.getSystemInstructions())
                .emoji(project.getEmoji())
                .color(project.getColor())
                .status(project.getStatus())
                .favorite(project.isFavorite())
                .conversationCount(convCount)
                .documentCount(docCount)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
