package com.digitalstamp.dto.user;

import com.digitalstamp.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private Role role;
    private boolean active;
    private String qrPayload;
    private Instant createdAt;
}
