-- V007: Agent Decisions — record of every agent reasoning output
CREATE TABLE agent_decisions (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    call_id      UUID        NOT NULL,
    tenant_id    UUID        NOT NULL,
    agent_type   VARCHAR(100) NOT NULL,
    capability   VARCHAR(100) NOT NULL,
    decision     VARCHAR(50)  NOT NULL,
    confidence   FLOAT        NOT NULL,
    rationale    TEXT,
    auto_enforced BOOLEAN    NOT NULL DEFAULT FALSE,
    decided_at   TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_agent_decisions PRIMARY KEY (id),
    CONSTRAINT fk_agent_decisions_call FOREIGN KEY (call_id) REFERENCES api_calls (id),
    CONSTRAINT fk_agent_decisions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT chk_agent_confidence CHECK (confidence >= 0.0 AND confidence <= 1.0),
    CONSTRAINT chk_agent_decision CHECK (decision IN ('ALLOW', 'BLOCK', 'ALERT', 'DEFER', 'SUGGEST'))
);

CREATE INDEX idx_agent_decisions_call ON agent_decisions (call_id);
CREATE INDEX idx_agent_decisions_tenant ON agent_decisions (tenant_id, decided_at DESC);
CREATE INDEX idx_agent_decisions_type ON agent_decisions (agent_type, decided_at DESC);

COMMENT ON TABLE agent_decisions IS 'Audit trail of every agent reasoning output with confidence and rationale';
COMMENT ON COLUMN agent_decisions.auto_enforced IS 'TRUE only if confidence >= 0.8 and action was automatically applied';
