package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.*;
import com.avcoe.lostfound.entity.*;
import com.avcoe.lostfound.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HandoverService {

    private final HandoverRepository handoverRepository;
    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final CampusLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public HandoverService(HandoverRepository handoverRepository, ClaimRepository claimRepository,
                           ItemRepository itemRepository, CampusLocationRepository locationRepository,
                           UserRepository userRepository, NotificationService notificationService) {
        this.handoverRepository = handoverRepository;
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public HandoverResponse createHandover(HandoverRequest request) {
        User user = getCurrentUser();
        Claim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));

        if (!claim.getItem().getReporter().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Not authorized to create handover");
        }

        if (claim.getItem().getItemType() != Item.ItemType.FOUND) {
            throw new IllegalArgumentException("Handovers can only be scheduled for found items");
        }

        if (claim.getStatus() != Claim.ClaimStatus.OWNER_VERIFIED) {
            throw new IllegalArgumentException("Claim must be verified before scheduling handover");
        }

        CampusLocation location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        Handover handover = Handover.builder()
                .claim(claim)
                .item(claim.getItem())
                .finder(claim.getItem().getReporter())
                .owner(claim.getClaimant())
                .location(location)
                .scheduledDateTime(request.getScheduledDateTime())
                .status(Handover.HandoverStatus.SCHEDULED)
                .build();
        handover = handoverRepository.save(handover);

        claim.setStatus(Claim.ClaimStatus.COMPLETED);
        claimRepository.save(claim);

        Item item = claim.getItem();
        item.setStatus(Item.ItemStatus.RETURNED);
        itemRepository.save(item);

        notificationService.createNotification(
                claim.getClaimant(),
                "Handover Scheduled",
                "A handover has been scheduled for \"" + item.getItemName() + "\" at " + location.getName(),
                Notification.NotificationType.HANDOVER_SCHEDULED,
                handover.getId(),
                "HANDOVER"
        );

        return mapToResponse(handover);
    }

    public List<HandoverResponse> getMyHandovers() {
        User user = getCurrentUser();
        return handoverRepository.findByFinderIdOrOwnerIdOrderByCreatedAtDesc(user.getId(), user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public HandoverResponse confirmHandover(Long handoverId) {
        User user = getCurrentUser();
        Handover handover = handoverRepository.findById(handoverId)
                .orElseThrow(() -> new IllegalArgumentException("Handover not found"));

        if (handover.getFinder().getId().equals(user.getId())) {
            handover.setFinderConfirmed(true);
        } else if (handover.getOwner().getId().equals(user.getId())) {
            handover.setOwnerConfirmed(true);
        } else {
            throw new IllegalArgumentException("Not authorized");
        }

        if (handover.isFinderConfirmed() && handover.isOwnerConfirmed()) {
            handover.setStatus(Handover.HandoverStatus.COMPLETED);

            Item item = handover.getItem();
            item.setStatus(Item.ItemStatus.RETURNED);
            itemRepository.save(item);

            notificationService.createNotification(
                    handover.getFinder(),
                    "Item Returned",
                    "The handover for \"" + item.getItemName() + "\" has been completed.",
                    Notification.NotificationType.ITEM_RETURNED,
                    handover.getId(),
                    "HANDOVER"
            );
        }

        handover = handoverRepository.save(handover);
        return mapToResponse(handover);
    }

    private HandoverResponse mapToResponse(Handover handover) {
        HandoverResponse response = new HandoverResponse();
        response.setId(handover.getId());
        response.setClaimId(handover.getClaim().getId());
        response.setItemId(handover.getItem().getId());
        response.setItemName(handover.getItem().getItemName());
        response.setFinderId(handover.getFinder().getId());
        response.setFinderName(handover.getFinder().getFullName());
        response.setOwnerId(handover.getOwner().getId());
        response.setOwnerName(handover.getOwner().getFullName());
        response.setLocationId(handover.getLocation().getId());
        response.setLocationName(handover.getLocation().getName());
        response.setScheduledDateTime(handover.getScheduledDateTime());
        response.setStatus(handover.getStatus().name());
        response.setFinderConfirmed(handover.isFinderConfirmed());
        response.setOwnerConfirmed(handover.isOwnerConfirmed());
        response.setCreatedAt(handover.getCreatedAt());
        return response;
    }
}
