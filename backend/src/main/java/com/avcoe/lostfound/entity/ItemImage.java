package com.avcoe.lostfound.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String fileName;

    private String contentType;
    private Long fileSize;

    @Column(nullable = false)
    private int sortOrder;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = true;

    private LocalDateTime createdAt;
}
