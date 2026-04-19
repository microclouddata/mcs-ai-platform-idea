package com.mcs.aiplatform.config;

import java.security.Principal;

public class JwtUserPrincipal implements Principal {

    private final String userId;
    private final String email;
    private final String name;

    public JwtUserPrincipal(String userId, String email, String name) {
        this.userId = userId;
        this.email  = email  != null ? email  : "";
        this.name   = name   != null ? name   : "";
    }

    @Override
    public String getName() { return userId; }

    public String getUserId() { return userId; }
    public String getEmail()  { return email; }
    public String getUserName() { return name; }
}
