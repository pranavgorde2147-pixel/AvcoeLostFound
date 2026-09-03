package com.avcoe.lostfound.service;

import com.avcoe.lostfound.entity.CampusLocation;
import com.avcoe.lostfound.repository.CampusLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    private final CampusLocationRepository locationRepository;

    public LocationService(CampusLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<CampusLocation> getActiveLocations() {
        return locationRepository.findByActiveTrue();
    }

    public CampusLocation getLocation(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
    }
}
