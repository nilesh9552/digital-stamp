package com.digitalstamp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "reward_redemptions", indexes = {
        @Index(name = "idx_redemption_customer", columnList = "customer_id"),
        @Index(name = "idx_redemption_shop", columnList = "shop_id"),
        @Index(name = "idx_redemption_code", columnList = "redemptionCode", unique = true),
        @Index(name = "idx_redemption_card", columnList = "loyalty_card_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loyalty_card_id", nullable = false)
    private LoyaltyCard loyaltyCard;

    @Column(nullable = false)
    private Instant redeemedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RedemptionStatus status;

    @Column(nullable = false, unique = true, length = 40)
    private String redemptionCode;

    @Column(nullable = false)
    @Builder.Default
    private int stampsConsumed = 0;

    @jakarta.persistence.PrePersist
    void onCreate() {
        if (redeemedAt == null) {
            redeemedAt = Instant.now();
        }
    }
}
