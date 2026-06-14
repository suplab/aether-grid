package com.suplab.aether.proxy.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.suplab.aether.proxy.repository.JdbcOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApiCallCaptureFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ApiCallCaptureFilter.class);
    private static final String CALL_START_ATTR = "AETHER_CALL_START";
    private static final String CALL_ID_ATTR = "AETHER_CALL_ID";

    private final JdbcOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ApiCallCaptureFilter(JdbcOutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public int getOrder() {
        return -50;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var callId = UUID.randomUUID();
        var startTime = Instant.now();
        exchange.getAttributes().put(CALL_START_ATTR, startTime);
        exchange.getAttributes().put(CALL_ID_ATTR, callId);

        return chain.filter(exchange)
                .doFinally(signal -> {
                    var ctx = (TenantContext) exchange.getAttributes().get(TenantContext.EXCHANGE_ATTR);
                    if (ctx == null) return;

                    var statusCode = exchange.getResponse().getStatusCode();
                    var latencyMs = java.time.Duration.between(startTime, Instant.now()).toMillis();
                    var request = exchange.getRequest();

                    Mono.fromRunnable(() -> publishCallEvent(
                            callId,
                            ctx,
                            request.getMethod().name(),
                            request.getPath().value(),
                            statusCode,
                            latencyMs
                    ))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            v -> {},
                            err -> log.error("Failed to publish ApiCall event for callId={}: {}", callId, err.getMessage())
                    );
                });
    }

    private void publishCallEvent(UUID callId, TenantContext ctx,
                                   String method, String path,
                                   HttpStatusCode statusCode, long latencyMs) {
        var outcome = resolveOutcome(statusCode);
        var payload = buildPayload(callId, ctx, method, path, statusCode, latencyMs, outcome);

        try {
            var json = objectMapper.writeValueAsString(payload);
            outboxRepository.save(
                    "api.call.recorded",
                    callId.toString(),
                    "aether.api.calls",
                    json
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise ApiCall payload for callId={}: {}", callId, e.getMessage());
        }
    }

    private Map<String, Object> buildPayload(UUID callId, TenantContext ctx,
                                              String method, String path,
                                              HttpStatusCode statusCode, long latencyMs,
                                              String outcome) {
        var map = new HashMap<String, Object>();
        map.put("callId", callId.toString());
        map.put("tenantId", ctx.tenantId().toString());
        map.put("method", method);
        map.put("path", path);
        map.put("responseCode", statusCode != null ? statusCode.value() : 0);
        map.put("latencyMs", latencyMs);
        map.put("outcome", outcome);
        map.put("capturedAt", Instant.now().toString());
        return map;
    }

    private String resolveOutcome(HttpStatusCode status) {
        if (status == null) return "UNKNOWN";
        if (status.is2xxSuccessful()) return "SUCCESS";
        if (status.value() == 429) return "BLOCKED";
        if (status.is5xxServerError()) return "FAILURE";
        return "UNKNOWN";
    }
}
