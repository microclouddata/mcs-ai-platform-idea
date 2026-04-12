package com.mcs.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter that runs on every request through the gateway.
 *
 * <ul>
 *   <li>Stamps a short {@code X-Request-Id} header so traces can be correlated
 *       across logs from the gateway and downstream services.</li>
 *   <li>Logs method, path, response status and elapsed time on completion.</li>
 * </ul>
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        long startTime = System.currentTimeMillis();

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-Request-Id", requestId)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build())
                .doFinally(signal -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("[{}] {} {} -> {} ({}ms)",
                            requestId,
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getPath(),
                            exchange.getResponse().getStatusCode(),
                            duration);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
