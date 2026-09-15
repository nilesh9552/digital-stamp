package com.digitalstamp.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OwnerCustomerRow {
    private Long customerId;
    private String name;
    private String email;
    private String mobile;
    private int currentStamps;
    private int totalStamps;
    private String lastActivity;
    private String status;
    private Long loyaltyCardId;
}
