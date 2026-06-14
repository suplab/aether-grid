-- V005: Policies — versioned governance rules stored as YAML
CREATE TABLE policies (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id    UUID        NOT NULL,
    name         VARCHAR(255) NOT NULL,
    description  TEXT,
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    yaml_content TEXT        NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    activated_at TIMESTAMP,
    superseded_at TIMESTAMP,

    CONSTRAINT pk_policies PRIMARY KEY (id),
    CONSTRAINT fk_policies_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    CONSTRAINT chk_policies_status CHECK (status IN ('DRAFT', 'ACTIVE', 'SUPERSEDED', 'ARCHIVED'))
);

CREATE UNIQUE INDEX idx_policies_active_per_tenant
    ON policies (tenant_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_policies_tenant_status ON policies (tenant_id, status);

COMMENT ON TABLE policies IS 'Versioned governance policies stored as SpEL-condition YAML';
COMMENT ON COLUMN policies.yaml_content IS 'Full policy YAML including rules with SpEL conditions, actions, and severity';
