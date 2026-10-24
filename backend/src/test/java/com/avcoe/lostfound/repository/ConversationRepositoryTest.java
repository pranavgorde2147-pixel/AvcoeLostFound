package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.Conversation;
import com.avcoe.lostfound.entity.Claim;
import com.avcoe.lostfound.entity.Item;
import com.avcoe.lostfound.entity.Message;
import com.avcoe.lostfound.entity.User;
import com.avcoe.lostfound.service.ConversationCleanupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ConversationCleanupService.class)
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationCleanupService cleanupService;

    @Test
    void expiredConversationIsNotAccessibleOrListed() {
        User reporter = saveUser("reporter@example.com");
        User claimant = saveUser("claimant@example.com");
        Item item = itemRepository.save(Item.builder()
                .itemName("Found keys")
                .itemType(Item.ItemType.FOUND)
                .category(Item.ItemCategory.KEYS)
                .dateOfEvent(LocalDateTime.now())
                .status(Item.ItemStatus.ACTIVE)
                .reporter(reporter)
                .build());
        Conversation conversation = conversationRepository.save(Conversation.builder()
                .item(item)
                .participants(new HashSet<>(Set.of(reporter, claimant)))
                .createdBy(claimant)
                .active(true)
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .build());

        assertThat(conversationRepository.isParticipant(conversation.getId(), claimant.getId())).isFalse();
        assertThat(conversationRepository.findByParticipantId(claimant.getId())).isEmpty();
    }

    @Test
    void cleanupDeletesExpiredChatMessagesAndConversationButKeepsClaim() {
        User reporter = saveUser("cleanup-reporter@example.com");
        User claimant = saveUser("cleanup-claimant@example.com");
        Item item = itemRepository.save(Item.builder()
                .itemName("Found wallet")
                .itemType(Item.ItemType.FOUND)
                .category(Item.ItemCategory.WALLETS)
                .dateOfEvent(LocalDateTime.now())
                .status(Item.ItemStatus.ACTIVE)
                .reporter(reporter)
                .build());
        Conversation conversation = conversationRepository.save(Conversation.builder()
                .item(item)
                .participants(new HashSet<>(Set.of(reporter, claimant)))
                .createdBy(claimant)
                .active(true)
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .build());
        Claim claim = claimRepository.save(Claim.builder()
                .item(item)
                .claimant(claimant)
                .conversation(conversation)
                .status(Claim.ClaimStatus.OWNER_VERIFIED)
                .build());
        conversation.setClaim(claim);
        conversationRepository.save(conversation);
        messageRepository.save(Message.builder()
                .conversation(conversation)
                .sender(claimant)
                .content("Hello")
                .messageType(Message.MessageType.TEXT)
                .build());

        cleanupService.deleteExpiredConversations();

        assertThat(conversationRepository.findById(conversation.getId())).isEmpty();
        assertThat(messageRepository.findByConversationIdOrderByCreatedAtAsc(
                conversation.getId(), PageRequest.of(0, 10))).isEmpty();
        assertThat(claimRepository.findById(claim.getId()).orElseThrow().getConversation()).isNull();
    }

    private User saveUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password("encoded-password")
                .fullName("Test User")
                .role(User.Role.STUDENT)
                .enabled(true)
                .build());
    }
}
