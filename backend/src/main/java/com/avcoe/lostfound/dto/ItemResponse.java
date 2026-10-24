package com.avcoe.lostfound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {
    private Long id;
    private String itemName;
    private String itemType;
    private String category;
    private String description;
    private LocalDateTime dateOfEvent;
    private String approximateTime;
    private Long locationId;
    private String locationName;
    private String locationDescription;
    private String status;
    private boolean listed;
    private Long reporterId;
    private String reporterName;
    private String reporterRole;
    private String identifyingCharacteristics;
    private String currentItemLocation;
    private String conditionDescription;
    private boolean hasVerificationQuestion;
    private String verificationQuestion;
    private List<ImageResponse> images;
    private int claimCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private double matchScore;
}
