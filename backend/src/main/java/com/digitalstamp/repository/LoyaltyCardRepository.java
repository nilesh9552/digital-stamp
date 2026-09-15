package com.digitalstamp.repository;

import com.digitalstamp.entity.LoyaltyCard;
import com.digitalstamp.entity.LoyaltyCardStatus;
import com.digitalstamp.entity.Shop;
import com.digitalstamp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoyaltyCardRepository extends JpaRepository<LoyaltyCard, Long> {
    Optional<LoyaltyCard> findByCustomerAndShop(User customer, Shop shop);
    Optional<LoyaltyCard> findByCustomer_IdAndShop_Id(Long customerId, Long shopId);
    List<LoyaltyCard> findByCustomer_Id(Long customerId);
    Page<LoyaltyCard> findByShop_Id(Long shopId, Pageable pageable);
    long countByShop_Id(Long shopId);
    long countByShop_IdAndStatus(Long shopId, LoyaltyCardStatus status);

    @Query("""
            select c from LoyaltyCard c
            join fetch c.customer
            join fetch c.shop
            join fetch c.loyaltyProgram
            where c.customer.id = :customerId
            """)
    List<LoyaltyCard> findDetailedByCustomerId(@Param("customerId") Long customerId);

    @Query("""
            select c from LoyaltyCard c
            where c.shop.id = :shopId
              and (:q is null or lower(c.customer.name) like lower(concat('%', :q, '%'))
                   or lower(c.customer.email) like lower(concat('%', :q, '%'))
                   or c.customer.mobile like concat('%', :q, '%'))
            """)
    Page<LoyaltyCard> searchShopCustomers(@Param("shopId") Long shopId, @Param("q") String q, Pageable pageable);

    void deleteByShop_Id(Long shopId);
}
