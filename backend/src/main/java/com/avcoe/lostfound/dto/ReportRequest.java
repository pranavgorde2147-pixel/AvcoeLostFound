package com.avcoe.lostfound.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotNull
    private Long itemId;
    @NotNull
    private String reason;
    private String description;
}
