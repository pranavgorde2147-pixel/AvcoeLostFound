package com.avcoe.lostfound.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Long referenceId;
    private String referenceType;
    private boolean read;
    private LocalDateTime createdAt;
}
