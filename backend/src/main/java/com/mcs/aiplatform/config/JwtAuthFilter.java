package com.mcs.aiplatform.config;

import com.mcs.aiplatform.auth.JwtService;
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
 * Authentication filter with two-path resolution:
 *
 * <ol>
 *   <li><b>Gateway path (production)</b> – the API Gateway validates the JWT and
 *       forwards trusted {@code X-User-Id} / {@code X-User-Role} headers. The backend
 *       trusts these headers and skips JWT re-validation, keeping auth logic in one place.</li>
 *   <li><b>Direct path (local dev)</b> – when the gateway is not in the call chain the
 *       filter falls back to standard {@code Authorization: Bearer <token>} JWT validation.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ── Path 1: trust gateway-injected headers ───────────────────────────
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            String role = request.getHeader("X-User-Role");
            String grantedRole = "ROLE_" + (role != null && !role.isBlank() ? role : "USER");

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            new JwtUserPrincipal(userId),
                            null,
                            List.of(new SimpleGrantedAuthority(grantedRole))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }

        // ── Path 2: fall back to direct JWT validation (local dev / no gateway) ─
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

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        new JwtUserPrincipal(jwtUserId),
                        null,
                        List.of(new SimpleGrantedAuthority(grantedRole))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
