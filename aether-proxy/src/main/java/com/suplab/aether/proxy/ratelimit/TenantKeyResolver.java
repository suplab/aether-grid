package com.suplab.aether.proxy.ratelimit;

import com.suplab.aether.proxy.filter.TenantContext;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class TenantKeyResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        var ctx = (TenantContext) exchange.getAttributes().get(TenantContext.EXCHANGE_ATTR);
        if (ctx != null) {
            return Mono.just(ctx.tenantId().toString());
        }
        // Fall back to IP address for unauthenticated requests (rejected by auth filter anyway)
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        var ip = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
        return Mono.just(ip);
    }
}
