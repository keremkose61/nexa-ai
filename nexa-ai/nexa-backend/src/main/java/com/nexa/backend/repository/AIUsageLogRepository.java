package com.nexa.backend.repository;

import com.nexa.backend.entity.AIUsageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AIUsageLogRepository extends JpaRepository<AIUsageLog, UUID> {
    Page<AIUsageLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    @Query("SELECT SUM(l.totalTokens) FROM AIUsageLog l WHERE l.user.id = :userId AND l.createdAt >= :since")
    Long sumTokensSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT l.modelName, COUNT(l) FROM AIUsageLog l WHERE l.user.id = :userId GROUP BY l.modelName ORDER BY COUNT(l) DESC")
    List<Object[]> getModelUsageStats(@Param("userId") UUID userId);
    
    long countByUserId(UUID userId);
}
