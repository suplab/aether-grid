-- V002: Endpoints — API endpoints registered by tenants for governance
CREATE TABLE endpoints (
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id    UUID         NOT NULL,
    name         VARCHAR(255) NOT NULL,
    base_url     VARCHAR(2048) NOT NULL,
    path_pattern VARCHAR(1024) NOT NULL DEFAULT '/**',
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_endpoints PRIMARY KEY (id),
    CONSTRAINT fk_endpoints_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);

CREATE INDEX idx_endpoints_tenant_id ON endpoints (tenant_id);
CREATE INDEX idx_endpoints_active ON endpoints (tenant_id, active);

COMMENT ON TABLE endpoints IS 'API endpoints registered by tenants under Aether governance';
COMMENT ON COLUMN endpoints.path_pattern IS 'Ant-style path pattern matched by the proxy router';
