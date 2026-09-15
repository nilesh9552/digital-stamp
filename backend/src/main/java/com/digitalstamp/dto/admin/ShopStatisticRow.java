package com.digitalstamp.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopStatisticRow {
    private Long shopId;
    private String shopName;
    private String slug;
    private long customers;
    private long stamps;
    private long redemptions;
    private boolean active;
}
