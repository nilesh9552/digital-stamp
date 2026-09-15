package com.digitalstamp.repository;

import com.digitalstamp.entity.LoyaltyProgram;
import com.digitalstamp.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyProgramRepository extends JpaRepository<LoyaltyProgram, Long> {
    Optional<LoyaltyProgram> findByShop(Shop shop);
    Optional<LoyaltyProgram> findByShop_Id(Long shopId);
    void deleteByShop_Id(Long shopId);
}
