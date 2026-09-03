package com.avcoe.lostfound.controller;

import com.avcoe.lostfound.dto.*;
import com.avcoe.lostfound.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<?> createClaim(@Valid @RequestBody ClaimRequest request) {
        try {
            ClaimResponse response = claimService.createClaim(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getItemClaims(@PathVariable Long itemId) {
        try {
            List<ClaimResponse> response = claimService.getItemClaims(itemId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<ClaimResponse>> getMyClaims() {
        List<ClaimResponse> response = claimService.getMyClaims();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{claimId}/status")
    public ResponseEntity<?> updateClaimStatus(@PathVariable Long claimId,
                                               @RequestBody ClaimStatusUpdateRequest request) {
        try {
            ClaimResponse response = claimService.updateClaimStatus(claimId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
