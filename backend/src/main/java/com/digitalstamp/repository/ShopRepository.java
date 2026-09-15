package com.digitalstamp.repository;

import com.digitalstamp.entity.Shop;
import com.digitalstamp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Optional<Shop> findByOwner(User owner);
    Optional<Shop> findByOwner_Id(Long ownerId);
    long countByActiveTrue();
    Page<Shop> findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(String name, String slug, Pageable pageable);

    @Query("select s from Shop s left join fetch s.owner where s.slug = :slug")
    Optional<Shop> findBySlugWithOwner(String slug);

    @Query("select s from Shop s left join fetch s.owner where s.id = :id")
    Optional<Shop> findByIdWithOwner(Long id);
}
