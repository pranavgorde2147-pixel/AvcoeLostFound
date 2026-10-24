package com.avcoe.lostfound.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClaimRequest {
    @NotNull
    private Long itemId;
    @NotBlank
    @Size(max = 1000)
    private String claimMessage;
}
