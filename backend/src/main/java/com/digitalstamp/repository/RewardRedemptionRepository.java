package com.digitalstamp.repository;

import com.digitalstamp.entity.RedemptionStatus;
import com.digitalstamp.entity.RewardRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, Long> {
    Page<RewardRedemption> findByCustomer_IdOrderByRedeemedAtDesc(Long customerId, Pageable pageable);
    Page<RewardRedemption> findByShop_IdOrderByRedeemedAtDesc(Long shopId, Pageable pageable);
    Page<RewardRedemption> findAllByOrderByRedeemedAtDesc(Pageable pageable);
    long countByShop_Id(Long shopId);
    long countByShop_IdAndRedeemedAtGreaterThanEqual(Long shopId, Instant from);
    long countByStatus(RedemptionStatus status);
    boolean existsByRedemptionCode(String code);
    void deleteByShop_Id(Long shopId);
}
