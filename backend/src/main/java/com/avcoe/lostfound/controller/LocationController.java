package com.avcoe.lostfound.controller;

import com.avcoe.lostfound.entity.CampusLocation;
import com.avcoe.lostfound.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<CampusLocation>> getLocations() {
        return ResponseEntity.ok(locationService.getActiveLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLocation(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(locationService.getLocation(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
