package com.mcs.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * Named gateway filter "JwtAuth".
 *
 * <p>Applied to protected routes in application.yml via {@code - JwtAuth}.
 * <ul>
 *   <li>Rejects requests without a valid Bearer token with HTTP 401.</li>
 *   <li>Forwards {@code X-User-Id}, {@code X-User-Email}, {@code X-User-Role}
 *       headers to downstream services so they can trust the caller identity
 *       without re-validating the JWT.</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Value("${app.jwt.secret:${APP_JWT_SECRET:cUVHUmhsQ1ReaGF1OWhlMzM5JHJyRjZuUEVYaXUlemt5JDdjJVpuYjdsQklIVndSVVVXSXNmS0FWSyZodUVqKg==}}")
    private String jwtSecret;

    public JwtAuthGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                ServerHttpRequest mutated = exchange.getRequest().mutate()
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Email", String.valueOf(claims.get("email")))
                        .header("X-User-Role", String.valueOf(claims.get("role")))
                        .build();

                return chain.filter(exchange.mutate().request(mutated).build());

            } catch (Exception e) {
                log.warn("JWT validation failed for {}: {}", exchange.getRequest().getPath(), e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }
}
