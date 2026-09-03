package com.avcoe.lostfound.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClaimResponse {
    private Long id;
    private Long itemId;
    private String itemName;
    private Long claimantId;
    private String claimantName;
    private String claimMessage;
    private String status;
    private Long conversationId;
    private boolean ownershipConfirmed;
    private LocalDateTime createdAt;
}
