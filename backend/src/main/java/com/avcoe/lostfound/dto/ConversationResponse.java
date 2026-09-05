package com.avcoe.lostfound.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationResponse {
    private Long id;
    private String title;
    private Long itemId;
    private String itemName;
    private String otherUserName;
    private List<ParticipantResponse> participants;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private LocalDateTime createdAt;
}
