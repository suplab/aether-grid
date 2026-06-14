package com.suplab.aether.proxy.filter;

import com.suplab.aether.core.domain.Tenant;
import com.suplab.aether.core.ports.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantAuthFilterTest {

    @Mock
    private TenantRepository tenantRepository;

    private TenantAuthFilter filter;

    private static final GatewayFilterChain NOOP_CHAIN = exchange -> Mono.empty();

    @BeforeEach
    void setUp() {
        filter = new TenantAuthFilter(tenantRepository);
    }

    @Test
    void rejects_missing_api_key_with_401() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/v1/data").build());

        filter.filter(exchange, NOOP_CHAIN).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rejects_unknown_api_key_with_401() {
        when(tenantRepository.findByApiKeyHash(anyString())).thenReturn(Optional.empty());

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/data").header("X-API-Key", "unknown").build());

        filter.filter(exchange, NOOP_CHAIN).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rejects_suspended_tenant_with_401() {
        var tenant = Tenant.onboard("Test Corp", "hash");
        tenant.suspend();
        when(tenantRepository.findByApiKeyHash(anyString())).thenReturn(Optional.of(tenant));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/data").header("X-API-Key", "suspended-key").build());

        filter.filter(exchange, NOOP_CHAIN).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void accepts_valid_active_tenant_and_stores_context() {
        var tenant = Tenant.onboard("Acme Corp", "sha256hash");
        when(tenantRepository.findByApiKeyHash(anyString())).thenReturn(Optional.of(tenant));

        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/v1/data").header("X-API-Key", "valid-key").build());

        filter.filter(exchange, NOOP_CHAIN).block();

        var ctx = (TenantContext) exchange.getAttributes().get(TenantContext.EXCHANGE_ATTR);
        assertThat(ctx).isNotNull();
        assertThat(ctx.tenant().name()).isEqualTo("Acme Corp");
    }

    @Test
    void actuator_paths_bypass_auth() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health").build());

        filter.filter(exchange, NOOP_CHAIN).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }
}
