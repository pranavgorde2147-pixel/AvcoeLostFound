package com.avcoe.lostfound.controller;

import com.avcoe.lostfound.dto.*;
import com.avcoe.lostfound.service.HandoverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/handovers")
public class HandoverController {

    private final HandoverService handoverService;

    public HandoverController(HandoverService handoverService) {
        this.handoverService = handoverService;
    }

    @PostMapping
    public ResponseEntity<?> createHandover(@Valid @RequestBody HandoverRequest request) {
        try {
            HandoverResponse response = handoverService.createHandover(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<HandoverResponse>> getMyHandovers() {
        List<HandoverResponse> response = handoverService.getMyHandovers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{handoverId}/confirm")
    public ResponseEntity<?> confirmHandover(@PathVariable Long handoverId) {
        try {
            HandoverResponse response = handoverService.confirmHandover(handoverId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
