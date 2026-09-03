package com.avcoe.lostfound.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 5000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Column(nullable = false)
    private boolean read = false;

    private LocalDateTime readAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum MessageType {
        TEXT, SYSTEM, VERIFICATION_QUESTION, VERIFICATION_ANSWER,
        HANDOVER_REQUEST, HANDOVER_CONFIRMED, ITEM_RETURNED
    }
}
