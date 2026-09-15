package com.digitalstamp.dto.auth;

import com.digitalstamp.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private String tokenType;
}
