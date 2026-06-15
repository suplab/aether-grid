package com.suplab.aether.proxy.filter;

import com.suplab.aether.core.ports.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class TenantAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TenantAuthFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";

    private final TenantRepository tenantRepository;

    public TenantAuthFilter(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var path = exchange.getRequest().getPath().value();
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        var apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Missing {} header on {}", API_KEY_HEADER, path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        var keyHash = sha256Hex(apiKey);
        return Mono.fromCallable(() -> tenantRepository.findByApiKeyHash(keyHash))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(opt -> {
                    if (opt.isEmpty() || !opt.get().isActive()) {
                        log.warn("Auth failed: unknown or inactive tenant for key hash prefix {}",
                                keyHash.substring(0, 8));
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    var tenant = opt.get();
                    exchange.getAttributes().put(TenantContext.EXCHANGE_ATTR, new TenantContext(tenant));
                    return chain.filter(exchange);
                });
    }

    private static String sha256Hex(String input) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
