-- V010: Tenant GDPR Preferences — memory opt-out flag and data retention policy
ALTER TABLE tenants
    ADD COLUMN IF NOT EXISTS memory_opt_out      BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS data_retention_days INT     NOT NULL DEFAULT 365;

COMMENT ON COLUMN tenants.memory_opt_out      IS 'When TRUE, no new memory embeddings are stored for this tenant (GDPR right to restrict processing)';
COMMENT ON COLUMN tenants.data_retention_days IS 'Days after which memory embeddings for this tenant are eligible for deletion';
