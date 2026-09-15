package com.digitalstamp.dto.stamp;

import com.digitalstamp.entity.StampTransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class StampTransactionResponse {
    private Long id;
    private Long loyaltyCardId;
    private Long customerId;
    private String customerName;
    private Long shopId;
    private String shopName;
    private int stampsAdded;
    private StampTransactionType transactionType;
    private String description;
    private Instant createdAt;
}
