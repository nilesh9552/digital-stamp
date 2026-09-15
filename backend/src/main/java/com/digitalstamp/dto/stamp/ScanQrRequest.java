package com.digitalstamp.dto.stamp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScanQrRequest {
    @NotBlank(message = "QR payload is required")
    private String qrPayload;
}
