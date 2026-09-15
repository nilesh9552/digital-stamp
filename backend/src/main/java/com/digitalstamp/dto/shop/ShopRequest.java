package com.digitalstamp.dto.shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopRequest {
    @NotBlank(message = "Shop name is required")
    @Size(max = 160)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 120)
    private String slug;

    private String logo;
    @Size(max = 2000)
    private String description;
    @Size(max = 500)
    private String address;
    private String phone;
    private String email;
    private String openingHours;
    private Boolean active;
    private Long ownerId;
    private String loyaltyProgramName;
    private String loyaltyProgramDescription;
    private Integer requiredStamps;
}
