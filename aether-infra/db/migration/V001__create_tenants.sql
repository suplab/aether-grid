-- V001: Tenants — organisations onboarded to Aether governance
CREATE TABLE tenants (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    api_key_hash VARCHAR(64)  NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uq_tenants_api_key_hash UNIQUE (api_key_hash),
    CONSTRAINT chk_tenants_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_tenants_status ON tenants (status);

COMMENT ON TABLE tenants IS 'Organisations that have onboarded API endpoints to Aether governance';
COMMENT ON COLUMN tenants.api_key_hash IS 'SHA-256 hash of the API key — never store plaintext';
