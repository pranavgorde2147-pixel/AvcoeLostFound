package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.ClaimRequest;
import com.avcoe.lostfound.dto.ClaimResponse;
import com.avcoe.lostfound.dto.ClaimStatusUpdateRequest;
import com.avcoe.lostfound.entity.Claim;
import com.avcoe.lostfound.entity.Conversation;
import com.avcoe.lostfound.entity.Item;
import com.avcoe.lostfound.entity.Message;
import com.avcoe.lostfound.entity.User;
import com.avcoe.lostfound.repository.ClaimRepository;
import com.avcoe.lostfound.repository.ConversationRepository;
import com.avcoe.lostfound.repository.ItemRepository;
import com.avcoe.lostfound.repository.MessageRepository;
import com.avcoe.lostfound.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClaimService claimService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void lostItemResponseIsSavedAsFirstPrivateChatMessage() {
        User reporter = User.builder()
                .id(1L)
                .email("reporter@example.com")
                .fullName("Reporter")
                .role(User.Role.STUDENT)
                .build();
        User respondent = User.builder()
                .id(2L)
                .email("respondent@example.com")
                .fullName("Respondent")
                .role(User.Role.STUDENT)
                .build();
        Item item = Item.builder()
                .id(10L)
                .itemName("Blue backpack")
                .itemType(Item.ItemType.LOST)
                .status(Item.ItemStatus.ACTIVE)
                .reporter(reporter)
                .build();
        ClaimRequest request = new ClaimRequest();
        request.setItemId(item.getId());
        request.setClaimMessage("I found it near the library.");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(respondent.getEmail(), "n/a"));

        when(userRepository.findByEmail(respondent.getEmail())).thenReturn(Optional.of(respondent));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(claimRepository.findByItemIdAndClaimantId(item.getId(), respondent.getId()))
                .thenReturn(Optional.empty());
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim claim = invocation.getArgument(0);
            claim.setId(20L);
            return claim;
        });
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation conversation = invocation.getArgument(0);
            conversation.setId(30L);
            return conversation;
        });
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response = claimService.createClaim(request);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        assertThat(response.getConversationId()).isEqualTo(30L);
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("I found it near the library.");
        assertThat(messageCaptor.getValue().getMessageType()).isEqualTo(Message.MessageType.VERIFICATION_ANSWER);
        assertThat(messageCaptor.getValue().getConversation().getItem().getItemType()).isEqualTo(Item.ItemType.LOST);
        assertThat(item.getStatus()).isEqualTo(Item.ItemStatus.ACTIVE);
    }

    @Test
    void reporterApprovalVerifiesClaimAndUnlocksItem() {
        User reporter = User.builder()
                .id(1L)
                .email("reporter@example.com")
                .fullName("Reporter")
                .role(User.Role.STUDENT)
                .build();
        User claimant = User.builder()
                .id(2L)
                .email("claimant@example.com")
                .fullName("Claimant")
                .role(User.Role.STUDENT)
                .build();
        Item item = Item.builder()
                .id(10L)
                .itemName("Found phone")
                .itemType(Item.ItemType.FOUND)
                .status(Item.ItemStatus.ACTIVE)
                .reporter(reporter)
                .build();
        Claim claim = Claim.builder()
                .id(20L)
                .item(item)
                .claimant(claimant)
                .status(Claim.ClaimStatus.VERIFICATION_IN_PROGRESS)
                .build();
        Conversation conversation = Conversation.builder()
                .id(30L)
                .claim(claim)
                .item(item)
                .build();
        claim.setConversation(conversation);
        ClaimStatusUpdateRequest request = new ClaimStatusUpdateRequest();
        request.setStatus("OWNER_VERIFIED");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(reporter.getEmail(), "n/a"));

        when(userRepository.findByEmail(reporter.getEmail())).thenReturn(Optional.of(reporter));
        when(claimRepository.findById(claim.getId())).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response = claimService.updateClaimStatus(claim.getId(), request);

        assertThat(response.getStatus()).isEqualTo(Claim.ClaimStatus.OWNER_VERIFIED.name());
        assertThat(response.isOwnershipConfirmed()).isTrue();
        assertThat(item.getStatus()).isEqualTo(Item.ItemStatus.ACTIVE);
        assertThat(conversation.getExpiresAt()).isAfter(LocalDateTime.now())
                .isBefore(LocalDateTime.now().plusHours(24));
        verify(conversationRepository).save(conversation);
        verify(itemRepository, never()).save(item);
    }
}
