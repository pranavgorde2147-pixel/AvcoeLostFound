package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.SendMessageRequest;
import com.avcoe.lostfound.entity.Claim;
import com.avcoe.lostfound.entity.Conversation;
import com.avcoe.lostfound.entity.Item;
import com.avcoe.lostfound.entity.User;
import com.avcoe.lostfound.repository.ConversationRepository;
import com.avcoe.lostfound.repository.ItemRepository;
import com.avcoe.lostfound.repository.MessageRepository;
import com.avcoe.lostfound.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatService chatService;

    @Test
    void doesNotSendMessageBeforeReporterApprovesRequest() {
        User reporter = User.builder().id(1L).email("reporter@example.com").fullName("Reporter").build();
        User claimant = User.builder().id(2L).email("claimant@example.com").fullName("Claimant").build();
        Item item = Item.builder()
                .id(10L)
                .itemName("Lost keys")
                .itemType(Item.ItemType.LOST)
                .reporter(reporter)
                .build();
        Claim claim = Claim.builder()
                .id(20L)
                .item(item)
                .status(Claim.ClaimStatus.VERIFICATION_IN_PROGRESS)
                .build();
        Conversation conversation = Conversation.builder()
                .id(30L)
                .claim(claim)
                .item(item)
                .participants(Set.of(reporter, claimant))
                .build();
        SendMessageRequest request = new SendMessageRequest();
        request.setContent("Hello");

        when(userRepository.findByEmail(claimant.getEmail())).thenReturn(Optional.of(claimant));
        when(conversationRepository.isParticipant(30L, claimant.getId())).thenReturn(true);
        when(conversationRepository.findById(30L)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> chatService.sendMessage(30L, request, claimant.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chat is locked");

        verify(messageRepository, never()).save(any());
    }
}
