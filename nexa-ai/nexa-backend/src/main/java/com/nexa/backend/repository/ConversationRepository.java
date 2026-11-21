package com.nexa.backend.repository;

import com.nexa.backend.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Page<Conversation> findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(UUID userId, Pageable pageable);
    Page<Conversation> findByUserIdAndFavoriteTrueAndArchivedFalseOrderByUpdatedAtDesc(UUID userId, Pageable pageable);
    
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId AND LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) AND c.archived = false ORDER BY c.updatedAt DESC")
    Page<Conversation> searchByTitle(@Param("userId") UUID userId, @Param("query") String query, Pageable pageable);
    
    Optional<Conversation> findByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
}
