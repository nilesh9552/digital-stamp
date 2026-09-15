package com.digitalstamp.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsResponse {
    private long totalShops;
    private long activeShops;
    private long totalCustomers;
    private long totalShopOwners;
    private long totalStamps;
    private long totalRewardsRedeemed;
    private long totalCustomersForShop;
    private long activeLoyaltyCards;
    private long stampsIssued;
    private long rewardsRedeemed;
    private long todayStamps;
    private long todayRedemptions;
    private String shopName;
}
