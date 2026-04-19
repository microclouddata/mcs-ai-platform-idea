package com.mcs.aiplatform.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {}

    public static String userId() {
        return principal().getUserId();
    }

    public static String email() {
        return principal().getEmail();
    }

    public static String name() {
        return principal().getUserName();
    }

    private static JwtUserPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Unauthenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal;
        }
        throw new RuntimeException("Invalid authentication principal");
    }
}
