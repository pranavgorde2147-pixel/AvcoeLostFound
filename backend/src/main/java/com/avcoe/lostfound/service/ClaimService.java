package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.*;
import com.avcoe.lostfound.entity.*;
import com.avcoe.lostfound.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NotificationService notificationService;

    public ClaimService(ClaimRepository claimRepository, ItemRepository itemRepository,
                        UserRepository userRepository, ConversationRepository conversationRepository,
                        MessageRepository messageRepository,
                        NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.notificationService = notificationService;
    }

    private User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public ClaimResponse createClaim(ClaimRequest request) {
        User claimant = getCurrentUser();
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (item.getStatus() != Item.ItemStatus.ACTIVE
                && item.getStatus() != Item.ItemStatus.CLAIM_IN_PROGRESS) {
            throw new IllegalArgumentException("This item is no longer accepting requests");
        }

        if (item.getReporter().getId().equals(claimant.getId())) {
            throw new IllegalArgumentException("Cannot respond to your own item");
        }

        if (request.getClaimMessage() == null || request.getClaimMessage().isBlank()) {
            throw new IllegalArgumentException("Please answer the verification question");
        }

        Optional<Claim> existingClaim = claimRepository.findByItemIdAndClaimantId(item.getId(), claimant.getId());
        if (existingClaim.isPresent()) {
            throw new IllegalArgumentException("You have already claimed this item");
        }

        Claim claim = Claim.builder()
                .item(item)
                .claimant(claimant)
                .claimMessage(request.getClaimMessage())
                .verificationAnswer(request.getClaimMessage())
                .status(Claim.ClaimStatus.VERIFICATION_IN_PROGRESS)
                .build();
        claim = claimRepository.save(claim);

        Conversation conversation = Conversation.builder()
                .claim(claim)
                .item(item)
                .participants(new HashSet<>(Arrays.asList(item.getReporter(), claimant)))
                .createdBy(claimant)
                .title("Claim: " + item.getItemName())
                .active(true)
                .lastMessageAt(LocalDateTime.now())
                .build();
        conversation = conversationRepository.save(conversation);
        claim.setConversation(conversation);
        claimRepository.save(claim);

        messageRepository.save(Message.builder()
                .conversation(conversation)
                .sender(claimant)
                .content(request.getClaimMessage().trim())
                .messageType(Message.MessageType.VERIFICATION_ANSWER)
                .read(false)
                .build());

        notificationService.createNotification(
                item.getReporter(),
                item.getItemType() == Item.ItemType.FOUND ? "New Claim on Your Found Item" : "Someone Found Your Lost Item",
                claimant.getFullName() + (item.getItemType() == Item.ItemType.FOUND
                        ? " thinks your found \"" : " may have found \"") + item.getItemName() + "\". Review their answer in chat.",
                Notification.NotificationType.CLAIM_RECEIVED,
                conversation.getId(),
                "CONVERSATION"
        );

        return mapToResponse(claim);
    }

    public List<ClaimResponse> getItemClaims(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        User user = getCurrentUser();

        if (!item.getReporter().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Not authorized to view these claims");
        }

        return claimRepository.findByItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ClaimResponse> getMyClaims() {
        User user = getCurrentUser();
        return claimRepository.findByClaimantIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClaimResponse updateClaimStatus(Long claimId, ClaimStatusUpdateRequest request) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        User user = getCurrentUser();

        if (!claim.getItem().getReporter().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Not authorized to update this claim");
        }

        Claim.ClaimStatus requestedStatus = null;
        if (request.getStatus() != null) {
            try {
                requestedStatus = Claim.ClaimStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid claim status");
            }
        }
        boolean approved = request.isOwnershipConfirmed()
                || requestedStatus == Claim.ClaimStatus.OWNER_VERIFIED;
        boolean rejected = requestedStatus == Claim.ClaimStatus.REJECTED;
        if (!approved && !rejected) {
            throw new IllegalArgumentException("Claim must be approved or declined");
        }
        if (claim.getStatus() == Claim.ClaimStatus.OWNER_VERIFIED
                || claim.getStatus() == Claim.ClaimStatus.REJECTED
                || claim.getStatus() == Claim.ClaimStatus.COMPLETED
                || claim.getStatus() == Claim.ClaimStatus.CANCELLED) {
            throw new IllegalArgumentException("This request has already been reviewed");
        }

        if (approved) {
            claim.setOwnershipConfirmed(true);
            claim.setStatus(Claim.ClaimStatus.OWNER_VERIFIED);
            claim.getItem().setStatus(Item.ItemStatus.OWNER_VERIFIED);
            itemRepository.save(claim.getItem());
        } else {
            claim.setStatus(Claim.ClaimStatus.REJECTED);
            if (claim.getItem().getStatus() == Item.ItemStatus.CLAIM_IN_PROGRESS) {
                claim.getItem().setStatus(Item.ItemStatus.ACTIVE);
                itemRepository.save(claim.getItem());
            }
        }

        notificationService.createNotification(
                claim.getClaimant(),
                approved ? "Request Approved" : "Request Declined",
                approved
                        ? "Your request about \"" + claim.getItem().getItemName() + "\" was approved. You can now chat with the reporter."
                            + (claim.getItem().getItemType() == Item.ItemType.FOUND
                                ? " A handover can also be scheduled." : "")
                        : "Your request about \"" + claim.getItem().getItemName() + "\" was declined.",
                approved ? Notification.NotificationType.OWNERSHIP_CONFIRMED : Notification.NotificationType.CLAIM_STATUS_CHANGED,
                claim.getId(),
                "CLAIM"
        );

        claim = claimRepository.save(claim);
        return mapToResponse(claim);
    }

    private ClaimResponse mapToResponse(Claim claim) {
        ClaimResponse response = new ClaimResponse();
        response.setId(claim.getId());
        response.setItemId(claim.getItem().getId());
        response.setItemName(claim.getItem().getItemName());
        response.setClaimantId(claim.getClaimant().getId());
        response.setClaimantName(claim.getClaimant().getFullName());
        response.setClaimMessage(claim.getClaimMessage());
        response.setStatus(claim.getStatus().name());
        response.setConversationId(claim.getConversation() != null ? claim.getConversation().getId() : null);
        response.setOwnershipConfirmed(claim.isOwnershipConfirmed());
        response.setCreatedAt(claim.getCreatedAt());
        return response;
    }
}
