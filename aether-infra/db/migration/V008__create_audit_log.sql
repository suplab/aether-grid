-- V008: Audit Log — immutable compliance record (append-only, no updates/deletes)
CREATE TABLE audit_log (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id    UUID,
    entity_type  VARCHAR(100) NOT NULL,
    entity_id    UUID,
    action       VARCHAR(100) NOT NULL,
    actor        VARCHAR(255) NOT NULL,
    detail       JSONB,
    occurred_at  TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_audit_log PRIMARY KEY (id)
    -- Intentionally no FK constraints — audit log must survive referenced entity deletion
);

CREATE INDEX idx_audit_log_tenant ON audit_log (tenant_id, occurred_at DESC);
CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id, occurred_at DESC);
CREATE INDEX idx_audit_log_actor ON audit_log (actor, occurred_at DESC);

COMMENT ON TABLE audit_log IS 'Immutable compliance audit log — append only, no FK constraints by design';
COMMENT ON COLUMN audit_log.detail IS 'JSONB payload with before/after state, policy YAML, redacted call data, etc.';
