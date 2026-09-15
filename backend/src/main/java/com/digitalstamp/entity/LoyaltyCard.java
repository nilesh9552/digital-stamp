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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loyalty_cards",
        uniqueConstraints = @UniqueConstraint(name = "uk_loyalty_card_customer_shop", columnNames = {"customer_id", "shop_id"}),
        indexes = {
                @Index(name = "idx_loyalty_card_shop", columnList = "shop_id"),
                @Index(name = "idx_loyalty_card_customer", columnList = "customer_id"),
                @Index(name = "idx_loyalty_card_status", columnList = "status")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyCard extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loyalty_program_id", nullable = false)
    private LoyaltyProgram loyaltyProgram;

    @Column(nullable = false)
    @Builder.Default
    private int currentStamps = 0;

    @Column(nullable = false)
    @Builder.Default
    private int totalStamps = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private LoyaltyCardStatus status = LoyaltyCardStatus.ACTIVE;
}
