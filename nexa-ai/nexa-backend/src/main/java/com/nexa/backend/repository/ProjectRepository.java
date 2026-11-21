package com.nexa.backend.repository;

import com.nexa.backend.entity.AIProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<AIProject, UUID> {
    Page<AIProject> findByUserIdAndStatusNotOrderByUpdatedAtDesc(UUID userId, AIProject.ProjectStatus status, Pageable pageable);
    Optional<AIProject> findByIdAndUserId(UUID id, UUID userId);
    
    @Query("SELECT p FROM AIProject p WHERE p.user.id = :userId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) AND p.status != 'DELETED' ORDER BY p.updatedAt DESC")
    Page<AIProject> searchByName(@Param("userId") UUID userId, @Param("query") String query, Pageable pageable);
    
    long countByUserId(UUID userId);
}
