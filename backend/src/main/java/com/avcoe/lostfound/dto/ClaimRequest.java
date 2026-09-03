package com.avcoe.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClaimRequest {
    @NotBlank
    private Long itemId;
    private String claimMessage;
}
