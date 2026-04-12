package com.mcs.rag.config;

import com.mcs.rag.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Two-path auth:
 * 1. Gateway path — trusts X-User-Id / X-User-Role headers injected by the gateway.
 * 2. Direct path — validates Bearer JWT for local dev without gateway.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Path 1: trust gateway-injected headers
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            String role = request.getHeader("X-User-Role");
            String grantedRole = "ROLE_" + (role != null && !role.isBlank() ? role : "USER");
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    new JwtUserPrincipal(userId), null,
                    List.of(new SimpleGrantedAuthority(grantedRole)));
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
            return;
        }

        // Path 2: direct JWT validation (local dev / no gateway)
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtUserId = jwtService.extractUserId(token);
        String jwtRole = jwtService.extractAllClaims(token).get("role", String.class);
        String grantedRole = "ROLE_" + (jwtRole != null ? jwtRole : "USER");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                new JwtUserPrincipal(jwtUserId), null,
                List.of(new SimpleGrantedAuthority(grantedRole)));
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
