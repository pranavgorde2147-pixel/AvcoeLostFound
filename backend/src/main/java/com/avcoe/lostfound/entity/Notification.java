package com.avcoe.lostfound.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private Long referenceId;
    private String referenceType;

    @Column(nullable = false)
    private boolean read = false;

    private LocalDateTime readAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum NotificationType {
        CLAIM_RECEIVED, MESSAGE_RECEIVED, VERIFICATION_SENT, VERIFICATION_ANSWERED,
        CLAIM_STATUS_CHANGED, OWNERSHIP_CONFIRMED, HANDOVER_SCHEDULED,
        ITEM_RETURNED, REPORT_RECEIVED, SYSTEM
    }
}
