package com.digitalstamp.dto.shop;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ShopResponse {
    private Long id;
    private String name;
    private String slug;
    private String logo;
    private String description;
    private String address;
    private String phone;
    private String email;
    private String openingHours;
    private boolean active;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String loyaltyProgramName;
    private String loyaltyProgramDescription;
    private Integer requiredStamps;
    private boolean loyaltyProgramActive;
    private Instant createdAt;
}
