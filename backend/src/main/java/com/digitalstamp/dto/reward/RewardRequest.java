package com.digitalstamp.dto.reward;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RewardRequest {
    @NotBlank
    private String name;
    private String description;
    @Min(1)
    private int requiredStamps;
    private Boolean active;
}
