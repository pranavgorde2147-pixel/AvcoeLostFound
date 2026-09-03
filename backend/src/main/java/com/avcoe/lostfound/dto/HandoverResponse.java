package com.avcoe.lostfound.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HandoverResponse {
    private Long id;
    private Long claimId;
    private Long itemId;
    private String itemName;
    private Long finderId;
    private String finderName;
    private Long ownerId;
    private String ownerName;
    private Long locationId;
    private String locationName;
    private LocalDateTime scheduledDateTime;
    private String status;
    private boolean finderConfirmed;
    private boolean ownerConfirmed;
    private LocalDateTime createdAt;
}
