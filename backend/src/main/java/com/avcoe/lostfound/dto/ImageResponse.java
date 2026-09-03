package com.avcoe.lostfound.dto;

import lombok.Data;

@Data
public class ImageResponse {
    private Long id;
    private String imageUrl;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private int sortOrder;
    private boolean primary;
}
