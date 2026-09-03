package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.CampusLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampusLocationRepository extends JpaRepository<CampusLocation, Long> {
    List<CampusLocation> findByActiveTrue();
    CampusLocation findByName(String name);
}
