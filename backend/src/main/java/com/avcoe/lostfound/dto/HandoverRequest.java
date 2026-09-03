package com.avcoe.lostfound.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HandoverRequest {
    @NotNull
    private Long claimId;
    @NotNull
    private Long locationId;
    @NotNull
    private LocalDateTime scheduledDateTime;
}
