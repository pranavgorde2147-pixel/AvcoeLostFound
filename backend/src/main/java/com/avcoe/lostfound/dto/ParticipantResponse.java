package com.avcoe.lostfound.dto;

import lombok.Data;

@Data
public class ParticipantResponse {
    private Long id;
    private String fullName;
    private String profileImageUrl;
    private String role;
}
