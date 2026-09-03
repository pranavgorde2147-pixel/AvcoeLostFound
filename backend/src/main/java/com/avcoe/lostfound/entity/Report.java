package com.avcoe.lostfound.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum ReportReason {
        FAKE_LISTING, SUSPICIOUS_ACTIVITY, WRONG_INFORMATION,
        DUPLICATE, INAPPROPRIATE_CONTENT, OTHER
    }

    public enum ReportStatus {
        PENDING, REVIEWED, RESOLVED, DISMISSED
    }
}
