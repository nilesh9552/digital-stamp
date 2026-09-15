package com.digitalstamp.dto.reward;

import com.digitalstamp.entity.RedemptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class RedemptionResponse {
    private Long id;
    private Long rewardId;
    private String rewardName;
    private Long customerId;
    private String customerName;
    private Long shopId;
    private String shopName;
    private Long loyaltyCardId;
    private Instant redeemedAt;
    private RedemptionStatus status;
    private String redemptionCode;
    private int stampsConsumed;
}
