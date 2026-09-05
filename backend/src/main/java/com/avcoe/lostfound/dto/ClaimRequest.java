package com.avcoe.lostfound.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimRequest {
    @NotNull
    private Long itemId;
    private String claimMessage;
}
