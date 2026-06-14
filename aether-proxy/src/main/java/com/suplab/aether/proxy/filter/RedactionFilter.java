package com.suplab.aether.proxy.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

public class RedactionFilter implements GlobalFilter, Ordered {

    private static final Set<String> REDACTED_HEADERS = Set.of(
            "Authorization",
            "X-API-Key",
            "X-Client-Secret",
            "Cookie",
            "Set-Cookie"
    );

    @Override
    public int getOrder() {
        return -90;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest sanitised = exchange.getRequest().mutate()
                .headers(headers -> REDACTED_HEADERS.forEach(headers::remove))
                .build();
        return chain.filter(exchange.mutate().request(sanitised).build());
    }
}
