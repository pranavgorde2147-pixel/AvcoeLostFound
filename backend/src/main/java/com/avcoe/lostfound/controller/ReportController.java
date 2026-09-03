package com.avcoe.lostfound.controller;

import com.avcoe.lostfound.dto.*;
import com.avcoe.lostfound.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportRequest request) {
        try {
            reportService.createReport(request.getItemId(), request.getReason(), request.getDescription());
            return ResponseEntity.ok(Map.of("message", "Report submitted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
