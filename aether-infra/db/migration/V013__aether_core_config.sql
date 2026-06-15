ALTER TABLE tenants
    ADD COLUMN IF NOT EXISTS aether_core_endpoint  TEXT,
    ADD COLUMN IF NOT EXISTS aether_core_api_key_hash TEXT;

COMMENT ON COLUMN tenants.aether_core_endpoint IS
    'Per-tenant AetherCore base URL override; NULL uses global aether.core.base-url';
COMMENT ON COLUMN tenants.aether_core_api_key_hash IS
    'SHA-256 hash of the per-tenant AetherCore API key; never store plaintext';
