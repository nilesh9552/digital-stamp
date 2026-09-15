package com.digitalstamp.security;

import com.digitalstamp.entity.User;
import com.digitalstamp.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw ApiException.unauthorized("Unauthorized access");
        }
        return user;
    }
}
