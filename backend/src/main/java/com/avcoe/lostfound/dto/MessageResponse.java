package com.avcoe.lostfound.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private String messageType;
    private boolean read;
    private LocalDateTime createdAt;
}
