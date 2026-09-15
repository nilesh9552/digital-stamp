package com.digitalstamp.dto.loyalty;

import com.digitalstamp.entity.LoyaltyCardStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoyaltyCardResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private String shopSlug;
    private String shopLogo;
    private Long programId;
    private String programName;
    private int currentStamps;
    private int totalStamps;
    private int requiredStamps;
    private int stampsRemaining;
    private boolean rewardEligible;
    private LoyaltyCardStatus status;
}
