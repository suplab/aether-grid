CREATE TABLE IF NOT EXISTS agent_feedback (
    id                   UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            UUID             NOT NULL,
    agent_type           VARCHAR(100)     NOT NULL,
    decision_id          UUID             NOT NULL,
    original_decision    VARCHAR(50)      NOT NULL,
    original_confidence  DOUBLE PRECISION NOT NULL,
    outcome              VARCHAR(50)      NOT NULL,
    outcome_detail       TEXT,
    recorded_at          TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agent_feedback_tenant_agent ON agent_feedback (tenant_id, agent_type, recorded_at DESC);
CREATE INDEX idx_agent_feedback_outcome ON agent_feedback (outcome, recorded_at DESC);

ALTER TABLE agent_feedback ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON agent_feedback
    USING (tenant_id::text = current_setting('app.tenant_id', true));
