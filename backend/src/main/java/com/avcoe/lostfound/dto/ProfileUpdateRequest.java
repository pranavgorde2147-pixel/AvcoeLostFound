package com.avcoe.lostfound.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private String department;
    private String rollNumber;
}
