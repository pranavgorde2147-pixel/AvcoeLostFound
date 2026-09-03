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
    private final NotificationService notificationService;

    public ClaimService(ClaimRepository claimRepository, ItemRepository itemRepository,
                        UserRepository userRepository, ConversationRepository conversationRepository,
                        NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
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

        if (item.getItemType() != Item.ItemType.FOUND) {
            throw new IllegalArgumentException("Can only claim FOUND items");
        }

        if (item.getReporter().getId().equals(claimant.getId())) {
            throw new IllegalArgumentException("Cannot claim your own item");
        }

        Optional<Claim> existingClaim = claimRepository.findByItemIdAndClaimantId(item.getId(), claimant.getId());
        if (existingClaim.isPresent()) {
            throw new IllegalArgumentException("You have already claimed this item");
        }

        Claim claim = Claim.builder()
                .item(item)
                .claimant(claimant)
                .claimMessage(request.getClaimMessage())
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

        item.setStatus(Item.ItemStatus.CLAIM_IN_PROGRESS);
        itemRepository.save(item);

        notificationService.createNotification(
                item.getReporter(),
                "New Claim on Your Found Item",
                claimant.getFullName() + " thinks your found \"" + item.getItemName() + "\" is theirs.",
                Notification.NotificationType.CLAIM_RECEIVED,
                claim.getId(),
                "CLAIM"
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

        if (request.getStatus() != null) {
            claim.setStatus(Claim.ClaimStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.isOwnershipConfirmed()) {
            claim.setOwnershipConfirmed(true);
            claim.setStatus(Claim.ClaimStatus.OWNER_VERIFIED);
            claim.getItem().setStatus(Item.ItemStatus.OWNER_VERIFIED);
            itemRepository.save(claim.getItem());

            notificationService.createNotification(
                    claim.getClaimant(),
                    "Ownership Verified!",
                    "Your claim for \"" + claim.getItem().getItemName() + "\" has been verified. A handover can now be scheduled.",
                    Notification.NotificationType.OWNERSHIP_CONFIRMED,
                    claim.getId(),
                    "CLAIM"
            );
        }

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
