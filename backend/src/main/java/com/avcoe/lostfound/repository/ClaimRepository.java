package com.avcoe.lostfound.repository;

import com.avcoe.lostfound.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByItemIdOrderByCreatedAtDesc(Long itemId);
    List<Claim> findByClaimantIdOrderByCreatedAtDesc(Long claimantId);
    Optional<Claim> findByItemIdAndClaimantId(Long itemId, Long claimantId);
    long countByItemId(Long itemId);
    long countByClaimantId(Long claimantId);
    long countByStatus(Claim.ClaimStatus status);
}
