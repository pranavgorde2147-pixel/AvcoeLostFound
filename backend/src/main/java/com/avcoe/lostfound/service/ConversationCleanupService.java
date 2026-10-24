package com.avcoe.lostfound.service;

import com.avcoe.lostfound.entity.Claim;
import com.avcoe.lostfound.entity.Conversation;
import com.avcoe.lostfound.repository.ClaimRepository;
import com.avcoe.lostfound.repository.ConversationRepository;
import com.avcoe.lostfound.repository.MessageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationCleanupService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ClaimRepository claimRepository;

    public ConversationCleanupService(ConversationRepository conversationRepository,
                                     MessageRepository messageRepository,
                                     ClaimRepository claimRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.claimRepository = claimRepository;
    }

    @Scheduled(fixedDelay = 60_000L)
    @Transactional
    public void deleteExpiredConversations() {
        List<Conversation> expiredConversations =
                conversationRepository.findByActiveTrueAndExpiresAtLessThanEqual(LocalDateTime.now());

        for (Conversation conversation : expiredConversations) {
            List<Claim> claims = claimRepository.findByConversationId(conversation.getId());
            claims.forEach(claim -> claim.setConversation(null));
            claimRepository.saveAll(claims);

            messageRepository.deleteByConversationId(conversation.getId());
            conversationRepository.delete(conversation);
        }
    }
}
