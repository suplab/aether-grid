-- V003: API Calls — every intercepted request/response pair
CREATE TABLE api_calls (
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id      UUID        NOT NULL,
    endpoint_id    UUID,
    http_method    VARCHAR(10)  NOT NULL,
    path           VARCHAR(2048) NOT NULL,
    request_hash   VARCHAR(64),
    response_code  INTEGER,
    latency_ms     BIGINT,
    outcome        VARCHAR(20)  NOT NULL DEFAULT 'UNKNOWN',
    captured_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_api_calls PRIMARY KEY (id),
    CONSTRAINT fk_api_calls_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_api_calls_endpoint FOREIGN KEY (endpoint_id) REFERENCES endpoints (id),
    CONSTRAINT chk_api_calls_outcome CHECK (outcome IN ('SUCCESS', 'FAILURE', 'TIMEOUT', 'BLOCKED', 'UNKNOWN'))
);

CREATE INDEX idx_api_calls_tenant_id ON api_calls (tenant_id);
CREATE INDEX idx_api_calls_tenant_captured ON api_calls (tenant_id, captured_at DESC);
CREATE INDEX idx_api_calls_endpoint_outcome ON api_calls (endpoint_id, outcome, captured_at DESC);

COMMENT ON TABLE api_calls IS 'Central fact table: every API call intercepted by the Aether proxy';
COMMENT ON COLUMN api_calls.request_hash IS 'SHA-256 of redacted request body for deduplication';
