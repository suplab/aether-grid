package com.suplab.aether.core.domain;

import com.suplab.aether.core.events.ApiCallRecordedEvent;
import com.suplab.aether.core.events.DomainEvent;

import java.util.ArrayList;
import java.util.List;

public final class ApiCall {

    private final ApiCallId id;
    private final TenantId tenantId;
    private final ApiEndpoint endpoint;
    private final HttpMethod method;
    private final String path;
    private final String requestHash;
    private CallOutcome outcome;
    private CallMetrics metrics;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private ApiCall(ApiCallId id, TenantId tenantId, ApiEndpoint endpoint,
                    HttpMethod method, String path, String requestHash) {
        this.id = id;
        this.tenantId = tenantId;
        this.endpoint = endpoint;
        this.method = method;
        this.path = path;
        this.requestHash = requestHash;
        this.outcome = CallOutcome.UNKNOWN;
    }

    public static ApiCall record(TenantId tenantId, ApiEndpoint endpoint,
                                  HttpMethod method, String path, String requestHash) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        if (endpoint == null) throw new IllegalArgumentException("endpoint must not be null");
        if (method == null) throw new IllegalArgumentException("method must not be null");
        if (path == null || path.isBlank()) throw new IllegalArgumentException("path must not be blank");

        var call = new ApiCall(ApiCallId.generate(), tenantId, endpoint, method, path, requestHash);
        call.domainEvents.add(new ApiCallRecordedEvent(call.id, tenantId, endpoint.id(), method, path));
        return call;
    }

    public void complete(CallMetrics metrics, CallOutcome outcome) {
        if (metrics == null) throw new IllegalArgumentException("metrics must not be null");
        if (outcome == null) throw new IllegalArgumentException("outcome must not be null");
        this.metrics = metrics;
        this.outcome = outcome;
    }

    public List<DomainEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public ApiCallId id() { return id; }
    public TenantId tenantId() { return tenantId; }
    public ApiEndpoint endpoint() { return endpoint; }
    public HttpMethod method() { return method; }
    public String path() { return path; }
    public String requestHash() { return requestHash; }
    public CallOutcome outcome() { return outcome; }
    public CallMetrics metrics() { return metrics; }
}
