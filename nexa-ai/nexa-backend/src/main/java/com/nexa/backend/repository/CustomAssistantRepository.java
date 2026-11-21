package com.nexa.backend.repository;

import com.nexa.backend.entity.CustomAssistant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomAssistantRepository extends JpaRepository<CustomAssistant, UUID> {
    Page<CustomAssistant> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<CustomAssistant> findByIsPublicTrue();
    Optional<CustomAssistant> findByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
}
