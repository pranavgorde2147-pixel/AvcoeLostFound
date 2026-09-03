package com.avcoe.lostfound.dto;

import lombok.Data;

@Data
public class ClaimStatusUpdateRequest {
    private String status;
    private boolean ownershipConfirmed;
}
