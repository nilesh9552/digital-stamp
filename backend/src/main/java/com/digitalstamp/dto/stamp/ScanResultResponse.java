package com.digitalstamp.dto.stamp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScanResultResponse {
    private Long customerId;
    private String customerName;
    private String email;
    private String mobile;
    private Long loyaltyCardId;
    private int currentStamps;
    private int requiredStamps;
    private int totalStamps;
    private boolean rewardEligible;
    private String shopName;
}
