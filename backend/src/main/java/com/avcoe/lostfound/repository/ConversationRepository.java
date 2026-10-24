package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId AND c.active = true AND (c.expiresAt IS NULL OR c.expiresAt > CURRENT_TIMESTAMP) ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByParticipantId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId AND c.item.id = :itemId AND c.active = true AND (c.expiresAt IS NULL OR c.expiresAt > CURRENT_TIMESTAMP)")
    Optional<Conversation> findByParticipantIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Conversation c JOIN c.participants p WHERE c.id = :conversationId AND p.id = :userId AND c.active = true AND (c.expiresAt IS NULL OR c.expiresAt > CURRENT_TIMESTAMP)")
    boolean isParticipant(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    List<Conversation> findByActiveTrueAndExpiresAtLessThanEqual(java.time.LocalDateTime expiresAt);
}
