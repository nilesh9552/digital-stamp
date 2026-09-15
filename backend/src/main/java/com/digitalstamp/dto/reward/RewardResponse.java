package com.digitalstamp.dto.reward;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RewardResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private Long loyaltyProgramId;
    private String name;
    private String description;
    private int requiredStamps;
    private boolean active;
    private boolean eligible;
}
