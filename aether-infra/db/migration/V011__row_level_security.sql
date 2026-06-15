-- V011: Row Level Security — tenant isolation enforced at PostgreSQL level
-- Uses current_setting('app.tenant_id', true) set by the application on each connection.
-- The second argument (true) makes missing setting return NULL instead of raising an error.

ALTER TABLE memory_embeddings ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON memory_embeddings
    USING (tenant_id::text = current_setting('app.tenant_id', true));

ALTER TABLE api_calls ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON api_calls
    USING (tenant_id::text = current_setting('app.tenant_id', true));

ALTER TABLE policies ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON policies
    USING (tenant_id::text = current_setting('app.tenant_id', true));

ALTER TABLE agent_decisions ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON agent_decisions
    USING (tenant_id::text = current_setting('app.tenant_id', true));

ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON audit_log
    USING (tenant_id::text = current_setting('app.tenant_id', true));

-- Protect tenants table: the API role cannot read rows belonging to other tenants.
-- FORCE ROW LEVEL SECURITY applies to table owners as well (prevents privilege escalation).
ALTER TABLE tenants FORCE ROW LEVEL SECURITY;
