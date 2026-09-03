package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatusOrderByCreatedAtDesc(Report.ReportStatus status, Pageable pageable);
    long countByStatus(Report.ReportStatus status);
    long countByItemId(Long itemId);
}
