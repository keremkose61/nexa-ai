package com.nexa.backend.repository;

import com.nexa.backend.entity.AIProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AIProviderEntityRepository extends JpaRepository<AIProviderEntity, UUID> {
    List<AIProviderEntity> findByUserIdAndEnabledTrue(UUID userId);
    List<AIProviderEntity> findByUserId(UUID userId);
    Optional<AIProviderEntity> findByIdAndUserId(UUID id, UUID userId);
    Optional<AIProviderEntity> findByUserIdAndDefaultProviderTrue(UUID userId);
}
