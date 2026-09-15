package com.digitalstamp.repository;

import com.digitalstamp.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findByShop_IdOrderByCreatedAtDesc(Long shopId);
    List<Reward> findByShop_IdAndActiveTrue(Long shopId);
    void deleteByShop_Id(Long shopId);
}
