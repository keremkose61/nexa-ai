package com.nexa.backend.service;

import com.nexa.backend.dto.response.UploadedFileResponse;
import com.nexa.backend.entity.AIProject;
import com.nexa.backend.entity.UploadedFile;
import com.nexa.backend.entity.User;
import com.nexa.backend.repository.ProjectRepository;
import com.nexa.backend.repository.UploadedFileRepository;
import com.nexa.backend.repository.UserRepository;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final UploadedFileRepository fileRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final Executor documentExecutor;

    @Value("${nexa.upload.dir:#{systemProperties['user.home'] + '/.nexa/uploads'}}")
    private String uploadDir;

    @Transactional
    public UploadedFileResponse uploadFile(UUID userId, UUID projectId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new NexaException(ErrorCode.FILE_UPLOAD_FAILED, "Cannot upload empty file");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NexaException(ErrorCode.USER_NOT_FOUND));

        AIProject project = null;
        if (projectId != null) {
            project = projectRepository.findByIdAndUserId(projectId, userId)
                    .orElse(null);
        }

        try {
            Path uploadPath = Paths.get(uploadDir, userId.toString());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String fileExtension = "";
            int dotIdx = originalFilename.lastIndexOf('.');
            if (dotIdx > 0) {
                fileExtension = originalFilename.substring(dotIdx);
            }

            String storedFilename = UUID.randomUUID() + fileExtension;
            Path destinationPath = uploadPath.resolve(storedFilename);

            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            UploadedFile uploadedFile = UploadedFile.builder()
                    .user(user)
                    .project(project)
                    .originalName(originalFilename)
                    .storedName(storedFilename)
                    .filePath(destinationPath.toAbsolutePath().toString())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .processingStatus(UploadedFile.ProcessingStatus.COMPLETED)
                    .build();

            uploadedFile = fileRepository.save(uploadedFile);
            log.info("Saved file {} for user {}", uploadedFile.getId(), userId);
            return mapToResponse(uploadedFile);
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw new NexaException(ErrorCode.FILE_UPLOAD_FAILED, "Failed to store file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public UploadedFile getFile(UUID userId, UUID fileId) {
        return fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.FILE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<UploadedFileResponse> getFiles(UUID userId, Pageable pageable) {
        return fileRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public void deleteFile(UUID userId, UUID fileId) {
        UploadedFile uploadedFile = fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.FILE_NOT_FOUND));

        try {
            Files.deleteIfExists(Paths.get(uploadedFile.getFilePath()));
        } catch (IOException e) {
            log.warn("Failed to delete physical file {}: {}", uploadedFile.getFilePath(), e.getMessage());
        }

        fileRepository.delete(uploadedFile);
        log.info("Deleted file record {} for user {}", fileId, userId);
    }

    public UploadedFileResponse mapToResponse(UploadedFile file) {
        return UploadedFileResponse.builder()
                .id(file.getId())
                .projectId(file.getProject() != null ? file.getProject().getId() : null)
                .originalName(file.getOriginalName())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .processingStatus(file.getProcessingStatus())
                .processingError(file.getProcessingError())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }
}
