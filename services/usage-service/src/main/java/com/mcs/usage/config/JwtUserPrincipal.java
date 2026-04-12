package com.mcs.usage.config;

import java.security.Principal;

public class JwtUserPrincipal implements Principal {

    private final String userId;

    public JwtUserPrincipal(String userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId;
    }

    public String getUserId() {
        return userId;
    }
}
