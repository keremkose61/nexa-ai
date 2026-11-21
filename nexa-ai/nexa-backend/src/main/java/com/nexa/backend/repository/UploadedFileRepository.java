package com.nexa.backend.repository;

import com.nexa.backend.entity.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {
    Page<UploadedFile> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Optional<UploadedFile> findByIdAndUserId(UUID id, UUID userId);
    List<UploadedFile> findByProjectId(UUID projectId);
    long countByUserId(UUID userId);
}
