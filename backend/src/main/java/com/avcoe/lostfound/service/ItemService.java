package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.*;
import com.avcoe.lostfound.entity.*;
import com.avcoe.lostfound.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository imageRepository;
    private final CampusLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final ClaimRepository claimRepository;

    public ItemService(ItemRepository itemRepository, ItemImageRepository imageRepository,
                       CampusLocationRepository locationRepository, UserRepository userRepository,
                       ClaimRepository claimRepository) {
        this.itemRepository = itemRepository;
        this.imageRepository = imageRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.claimRepository = claimRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public ItemResponse createItem(CreateItemRequest request) {
        User user = getCurrentUser();

        CampusLocation location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId()).orElse(null);
        }

        Item.ItemType itemType = Item.ItemType.valueOf(request.getItemType().toUpperCase());
        Item.ItemCategory category = Item.ItemCategory.valueOf(request.getCategory().toUpperCase());
        Item.ItemStatus status = Item.ItemStatus.ACTIVE;

        Item item = Item.builder()
                .itemName(request.getItemName())
                .itemType(itemType)
                .category(category)
                .description(request.getDescription())
                .dateOfEvent(request.getDateOfEvent())
                .approximateTime(request.getApproximateTime())
                .location(location)
                .locationDescription(request.getLocationDescription())
                .status(status)
                .reporter(user)
                .identifyingCharacteristics(request.getIdentifyingCharacteristics())
                .currentItemLocation(request.getCurrentItemLocation())
                .conditionDescription(request.getConditionDescription())
                .verificationQuestion(itemType == Item.ItemType.FOUND ? request.getVerificationQuestion() : null)
                .build();

        item = itemRepository.save(item);
        return mapToResponse(item);
    }

    public ItemResponse getItem(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        return mapToResponse(item);
    }

    public PaginatedResponse<ItemResponse> getItems(String type, String category, String status,
                                                     String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Item> itemPage;

        if (search != null && !search.isEmpty()) {
            if (type != null && !type.isEmpty()) {
                Item.ItemType itemType = Item.ItemType.valueOf(type.toUpperCase());
                itemPage = itemRepository.searchActiveItemsByType(search, itemType, pageable);
            } else {
                itemPage = itemRepository.searchActiveItems(search, pageable);
            }
        } else if (type != null && !type.isEmpty()) {
            Item.ItemType itemType = Item.ItemType.valueOf(type.toUpperCase());
            if (status != null && !status.isEmpty()) {
                Item.ItemStatus itemStatus = Item.ItemStatus.valueOf(status.toUpperCase());
                itemPage = itemRepository.findByItemTypeAndStatus(itemType, itemStatus, pageable);
            } else {
                itemPage = itemRepository.findByItemType(itemType, pageable);
            }
        } else if (status != null && !status.isEmpty()) {
            Item.ItemStatus itemStatus = Item.ItemStatus.valueOf(status.toUpperCase());
            itemPage = itemRepository.findByStatus(itemStatus, pageable);
        } else if (category != null && !category.isEmpty()) {
            Item.ItemCategory itemCategory = Item.ItemCategory.valueOf(category.toUpperCase());
            itemPage = itemRepository.findByCategory(itemCategory, pageable);
        } else {
            itemPage = itemRepository.findAll(pageable);
        }

        List<ItemResponse> content = itemPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.of(content, page, size, itemPage.getTotalElements());
    }

    public List<ItemResponse> getMyItems(String type) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> items;

        if (type != null && !type.isEmpty()) {
            Item.ItemType itemType = Item.ItemType.valueOf(type.toUpperCase());
            items = itemRepository.findByItemType(itemType, pageable);
        } else {
            items = itemRepository.findByReporter(user, pageable);
        }

        return items.getContent().stream()
                .filter(i -> i.getReporter().getId().equals(user.getId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ItemResponse> getPossibleMatches(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        List<Item> matches = itemRepository.findPossibleMatches(item.getItemName());
        return matches.stream()
                .filter(m -> !m.getId().equals(itemId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id).orElseThrow();
        User user = getCurrentUser();
        if (!item.getReporter().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Not authorized to delete this item");
        }
        item.setStatus(Item.ItemStatus.CLOSED);
        itemRepository.save(item);
    }

    private ItemResponse mapToResponse(Item item) {
        List<ItemImage> images = imageRepository.findByItemIdOrderBySortOrderAsc(item.getId());
        long claimCount = claimRepository.countByItemId(item.getId());

        return ItemResponse.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .itemType(item.getItemType().name())
                .category(item.getCategory().name())
                .description(item.getDescription())
                .dateOfEvent(item.getDateOfEvent())
                .approximateTime(item.getApproximateTime())
                .locationId(item.getLocation() != null ? item.getLocation().getId() : null)
                .locationName(item.getLocation() != null ? item.getLocation().getName() : item.getLocationDescription())
                .locationDescription(item.getLocationDescription())
                .status(item.getStatus().name())
                .reporterId(item.getReporter().getId())
                .reporterName(item.getReporter().getFullName())
                .reporterRole(item.getReporter().getRole().name())
                .identifyingCharacteristics(item.getIdentifyingCharacteristics())
                .currentItemLocation(item.getCurrentItemLocation())
                .conditionDescription(item.getConditionDescription())
                .hasVerificationQuestion(item.getVerificationQuestion() != null && !item.getVerificationQuestion().isEmpty())
                .images(images.stream().map(img -> {
                    ImageResponse ir = new ImageResponse();
                    ir.setId(img.getId());
                    ir.setImageUrl(img.getImageUrl());
                    ir.setFileName(img.getFileName());
                    ir.setContentType(img.getContentType());
                    ir.setFileSize(img.getFileSize());
                    ir.setSortOrder(img.getSortOrder());
                    ir.setPrimary(img.isPrimary());
                    return ir;
                }).collect(Collectors.toList()))
                .claimCount((int) claimCount)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
