package com.nexa.backend.repository;

import com.nexa.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId, Pageable pageable);
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :convId AND m.conversation.user.id = :userId")
    Page<Message> findByConversationIdAndUserId(@Param("convId") UUID convId, @Param("userId") UUID userId, Pageable pageable);
    
    long countByConversationId(UUID conversationId);
    
    @Query("SELECT SUM(m.totalTokens) FROM Message m WHERE m.conversation.user.id = :userId")
    Optional<Long> sumTokensByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);
}
