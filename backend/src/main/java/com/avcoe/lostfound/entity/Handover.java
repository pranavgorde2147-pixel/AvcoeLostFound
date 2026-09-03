package com.avcoe.lostfound.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "handovers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Handover {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finder_id", nullable = false)
    private User finder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private CampusLocation location;

    @Column(nullable = false)
    private LocalDateTime scheduledDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandoverStatus status;

    private boolean finderConfirmed = false;
    private boolean ownerConfirmed = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum HandoverStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
