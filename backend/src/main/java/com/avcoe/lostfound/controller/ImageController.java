package com.avcoe.lostfound.controller;

import com.avcoe.lostfound.dto.ImageResponse;
import com.avcoe.lostfound.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/item/{itemId}")
    public ResponseEntity<?> uploadImages(@PathVariable Long itemId, @RequestParam("files") MultipartFile[] files) {
        try {
            List<ImageResponse> responses = imageService.uploadImages(itemId, files);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<ImageResponse>> getItemImages(@PathVariable Long itemId) {
        List<ImageResponse> responses = imageService.getItemImages(itemId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.ok(Map.of("message", "Image deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
