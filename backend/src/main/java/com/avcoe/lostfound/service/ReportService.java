package com.avcoe.lostfound.service;

import com.avcoe.lostfound.entity.*;
import com.avcoe.lostfound.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public void createReport(Long itemId, String reason, String description) {
        User user = getCurrentUser();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        Report report = Report.builder()
                .reporter(user)
                .item(item)
                .reason(Report.ReportReason.valueOf(reason.toUpperCase().replace(" ", "_")))
                .description(description)
                .status(Report.ReportStatus.PENDING)
                .build();

        reportRepository.save(report);
    }
}
