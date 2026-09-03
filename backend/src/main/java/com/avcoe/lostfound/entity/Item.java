package com.avcoe.lostfound.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateOfEvent;

    private String approximateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private CampusLocation location;

    @Column(length = 500)
    private String locationDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    private String identifyingCharacteristics;

    // Found item specific
    private String currentItemLocation;
    private String conditionDescription;
    private String verificationQuestion;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ItemType {
        LOST, FOUND
    }

    public enum ItemCategory {
        ELECTRONICS, BAGS, BOOKS, ID_CARDS, WALLETS, KEYS, DOCUMENTS,
        CLOTHING, ACCESSORIES, STATIONERY, OTHER
    }

    public enum ItemStatus {
        ACTIVE, CLAIM_IN_PROGRESS, OWNER_VERIFIED, RETURNED, CLOSED
    }
}
