package com.digitalstamp.dto.stamp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StampActionRequest {
    @NotNull
    private Long customerId;
    @Min(1)
    private int stamps = 1;
    private String description;
}
