package com.avcoe.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateItemRequest {
    @NotBlank
    private String itemName;
    @NotNull
    private String itemType;
    @NotNull
    private String category;
    private String description;
    @NotNull
    private LocalDateTime dateOfEvent;
    private String approximateTime;
    private Long locationId;
    private String locationDescription;
    private String identifyingCharacteristics;
    private String currentItemLocation;
    private String conditionDescription;
    private String verificationQuestion;
}
