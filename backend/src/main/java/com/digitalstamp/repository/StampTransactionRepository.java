package com.digitalstamp.repository;

import com.digitalstamp.entity.StampTransaction;
import com.digitalstamp.entity.StampTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface StampTransactionRepository extends JpaRepository<StampTransaction, Long> {
    Page<StampTransaction> findByCustomer_IdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    Page<StampTransaction> findByShop_IdOrderByCreatedAtDesc(Long shopId, Pageable pageable);
    Page<StampTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select coalesce(sum(t.stampsAdded),0) from StampTransaction t where t.transactionType = com.digitalstamp.entity.StampTransactionType.ADD")
    long sumAllAddedStamps();

    @Query("select coalesce(sum(t.stampsAdded),0) from StampTransaction t where t.shop.id = :shopId and t.transactionType = :type")
    long sumByShopAndType(@Param("shopId") Long shopId, @Param("type") StampTransactionType type);

    @Query("select coalesce(sum(t.stampsAdded),0) from StampTransaction t where t.shop.id = :shopId and t.transactionType = :type and t.createdAt >= :from")
    long sumByShopTypeSince(@Param("shopId") Long shopId, @Param("type") StampTransactionType type, @Param("from") Instant from);

    long countByShop_IdAndTransactionTypeAndCreatedAtGreaterThanEqual(Long shopId, StampTransactionType type, Instant from);
    void deleteByShop_Id(Long shopId);
}
