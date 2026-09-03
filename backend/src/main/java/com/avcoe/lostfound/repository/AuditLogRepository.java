package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
