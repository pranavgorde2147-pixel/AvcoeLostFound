package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.Handover;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HandoverRepository extends JpaRepository<Handover, Long> {
    List<Handover> findByFinderIdOrOwnerIdOrderByCreatedAtDesc(Long finderId, Long ownerId);
    Optional<Handover> findByClaimId(Long claimId);
    Optional<Handover> findByItemId(Long itemId);
}
