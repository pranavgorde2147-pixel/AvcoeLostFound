package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);
    void deleteByConversationId(Long conversationId);

    @Modifying
    @Query("UPDATE Message m SET m.read = true, m.readAt = CURRENT_TIMESTAMP WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.read = false")
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    long countByConversationIdAndReadFalseAndSenderIdNot(Long conversationId, Long senderId);

    @Query("SELECT COALESCE(SUM(CASE WHEN m.read = false AND m.sender.id != :userId THEN 1 ELSE 0 END), 0) FROM Message m JOIN m.conversation c JOIN c.participants p WHERE p.id = :userId")
    long countUnreadByUserId(@Param("userId") Long userId);
}
