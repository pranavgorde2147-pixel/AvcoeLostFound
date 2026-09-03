package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.*;
import com.avcoe.lostfound.entity.*;
import com.avcoe.lostfound.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final ReportRepository reportRepository;

    public AdminService(UserRepository userRepository, ItemRepository itemRepository,
                        ClaimRepository claimRepository, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
        this.reportRepository = reportRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeLostItems", itemRepository.countByItemTypeAndStatus(Item.ItemType.LOST, Item.ItemStatus.ACTIVE));
        stats.put("activeFoundItems", itemRepository.countByItemTypeAndStatus(Item.ItemType.FOUND, Item.ItemStatus.ACTIVE));
        stats.put("returnedItems", itemRepository.countByStatus(Item.ItemStatus.RETURNED));
        stats.put("openClaims", claimRepository.countByStatus(Claim.ClaimStatus.PENDING));
        stats.put("pendingReports", reportRepository.countByStatus(Report.ReportStatus.PENDING));
        return stats;
    }

    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("fullName", user.getFullName());
            map.put("email", user.getEmail());
            map.put("role", user.getRole().name());
            map.put("department", user.getDepartment());
            map.put("rollNumber", user.getRollNumber());
            map.put("enabled", user.isEnabled());
            map.put("accountLocked", user.isAccountLocked());
            map.put("createdAt", user.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    public PaginatedResponse<Map<String, Object>> getReports(int page, int size) {
        var reportPage = reportRepository.findByStatusOrderByCreatedAtDesc(
                Report.ReportStatus.PENDING, PageRequest.of(page, size));

        List<Map<String, Object>> content = reportPage.getContent().stream().map(report -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", report.getId());
            map.put("reporterName", report.getReporter().getFullName());
            map.put("itemName", report.getItem().getItemName());
            map.put("reason", report.getReason().name());
            map.put("description", report.getDescription());
            map.put("status", report.getStatus().name());
            map.put("createdAt", report.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return PaginatedResponse.of(content, page, size, reportPage.getTotalElements());
    }
}
