-- V006: Policy Versions — immutable history of all policy changes
CREATE TABLE policy_versions (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    policy_id    UUID        NOT NULL,
    tenant_id    UUID        NOT NULL,
    version      INTEGER     NOT NULL,
    yaml_content TEXT        NOT NULL,
    changed_by   VARCHAR(255) NOT NULL,
    change_reason TEXT,
    changed_at   TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_policy_versions PRIMARY KEY (id),
    CONSTRAINT fk_policy_versions_policy FOREIGN KEY (policy_id) REFERENCES policies (id) ON DELETE CASCADE,
    CONSTRAINT fk_policy_versions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uq_policy_versions_number UNIQUE (policy_id, version)
);

CREATE INDEX idx_policy_versions_policy ON policy_versions (policy_id, version DESC);
CREATE INDEX idx_policy_versions_tenant ON policy_versions (tenant_id, changed_at DESC);

COMMENT ON TABLE policy_versions IS 'Immutable version history for every policy — append only';
