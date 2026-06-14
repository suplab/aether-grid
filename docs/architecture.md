# Aether — Technical Architecture

> Living document. Updated with each phase completion. Last updated: Phase 0.

---

## Overview

AetherGrid is implemented as a **Maven multi-module, modular-monolith** — two deployable Spring Boot applications backed by six library modules. Module boundaries enforce DDD bounded contexts at compile time. Future service extraction is possible without architectural surgery because ports and adapters are already separated.

**Two runnable applications:**
- `aether-proxy` — Data Plane (port 8080): Spring Cloud Gateway intercepting all API traffic
- `aether-api` — Control Plane (port 8081): Admin REST API for governance and configuration

**Five library modules** (JAR, no `main` class):
- `aether-core` — shared domain model, events, port interfaces
- `aether-memory` — embedding and vector storage
- `aether-agents` — agent SPI, registry, orchestrator, all agent implementations
- `aether-policy` — policy engine, rule evaluation, GDPR redaction
- `aether-infra` — Docker, Flyway migrations, K8s manifests (no Java)

---

## Module Dependency Graph

```
aether-proxy  ────────────────────────────────────┐
      │                                            │
      ▼                                            ▼
aether-core ◄── aether-memory ◄── aether-agents ──► aether-policy
                                                       │
                                                  aether-api
```

`aether-core` has **no dependencies** on other modules. All other modules depend on it. `aether-proxy` and `aether-api` depend on the full stack.

---

## Core Design Patterns

### Hexagonal Architecture (Ports & Adapters)
All outbound I/O (database, Kafka, Ollama, Redis) is behind interfaces defined in `aether-core`:

```java
// aether-core: port (interface only, no implementation)
public interface MemoryStore {
    void store(MemoryRecord record);
    List<MemoryRecord> findSimilar(EmbeddingVector query, int topK);
}

// aether-memory: adapter (implements the port)
@Component
public class PgVectorMemoryStore implements MemoryStore { ... }
```

Tests use in-memory stubs. The proxy has no knowledge of whether storage is PGVector or Chroma.

### Domain Events (Sealed Hierarchy)
All cross-module communication goes through sealed domain events on the Kafka bus:

```java
public sealed interface DomainEvent
    permits ApiCallRecordedEvent, PolicyViolatedEvent,
            AgentDecisionEvent, GovernanceUpdatedEvent { }
```

Java 21 pattern matching enables exhaustive handling — the compiler enforces that all event types are handled.

### Agent Plugin Pattern (SPI)
Agents are registered via Spring's constructor-injected `List<Agent>`:

```java
@Component
public class AgentRegistry {
    private final List<Agent> agents; // Spring injects all @Component Agent impls

    public AgentRegistry(List<Agent> agents) {
        this.agents = agents;
    }

    public List<Agent> findByCapability(AgentCapability capability) {
        return agents.stream()
            .filter(a -> a.canHandle(capability))
            .toList();
    }
}
```

Adding a new agent = implement `Agent` + `@Component`. Zero configuration.

### Transactional Outbox (Reliable Event Publishing)
Direct `KafkaTemplate.send()` inside a transaction creates dual-write risk. AetherGrid uses the outbox pattern:

```
DB Transaction:
  INSERT INTO api_calls ...
  INSERT INTO outbox_events (event_type, payload, published=false)
  COMMIT

Relay thread (separate process):
  SELECT * FROM outbox_events WHERE published=false
  KafkaTemplate.send(...)
  UPDATE outbox_events SET published=true
```

Ensures events are never lost even if Kafka is temporarily unavailable.

### Policy-as-Code (SpEL in YAML)
Governance rules are stored as YAML in PostgreSQL. Rules use Spring EL expressions evaluated at runtime:

```yaml
# Example policy stored in policies table
id: tenant-a-latency-policy
version: 3
rules:
  - name: high-latency-alert
    condition: "#call.metrics.latencyMs > 2000 && #call.outcome == 'FAILURE'"
    action: ALERT
    severity: HIGH
  - name: block-excessive-errors
    condition: "#call.metrics.errorRate > 0.3"
    action: BLOCK
    severity: CRITICAL
```

No redeployment needed to change governance rules.

---

## Data Model

### Core Tables (Flyway migrations V001–V009)

```
tenants
  id UUID PK
  name VARCHAR
  api_key_hash VARCHAR
  status VARCHAR
  created_at TIMESTAMP

api_calls
  id UUID PK
  tenant_id UUID FK → tenants.id
  endpoint_id UUID FK → endpoints.id
  method VARCHAR
  request_hash VARCHAR
  response_code INT
  latency_ms BIGINT
  outcome VARCHAR  -- SUCCESS | FAILURE | TIMEOUT
  captured_at TIMESTAMP

memory_embeddings
  id UUID PK
  tenant_id UUID FK
  memory_type VARCHAR  -- EPISODIC | SEMANTIC | PROCEDURAL | EMOTIONAL
  content TEXT
  embedding vector(384)  -- pgvector column, all-MiniLM-L6-v2 output
  strength FLOAT         -- 0.0–1.0, reinforced on retrieval
  last_accessed TIMESTAMP
  created_at TIMESTAMP

policies
  id UUID PK
  tenant_id UUID FK
  status VARCHAR  -- DRAFT | ACTIVE | SUPERSEDED
  yaml_content TEXT
  created_at TIMESTAMP
  activated_at TIMESTAMP

policy_versions
  id UUID PK
  policy_id UUID FK → policies.id
  version INT
  yaml_content TEXT
  changed_by VARCHAR
  changed_at TIMESTAMP

agent_decisions
  id UUID PK
  call_id UUID FK → api_calls.id
  agent_type VARCHAR
  capability VARCHAR
  decision VARCHAR
  confidence FLOAT
  rationale TEXT
  decided_at TIMESTAMP

audit_log
  id UUID PK
  tenant_id UUID FK
  entity_type VARCHAR
  entity_id UUID
  action VARCHAR
  actor VARCHAR
  detail JSONB
  occurred_at TIMESTAMP

outbox_events
  id UUID PK
  event_type VARCHAR
  payload JSONB
  published BOOLEAN DEFAULT false
  created_at TIMESTAMP
  published_at TIMESTAMP

endpoints
  id UUID PK
  tenant_id UUID FK
  name VARCHAR
  base_url VARCHAR
  path_pattern VARCHAR
  active BOOLEAN
```

### Vector Index
```sql
-- Cosine similarity index on memory_embeddings.embedding
CREATE INDEX ON memory_embeddings USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);
```

---

## API Proxy Flow

```
Inbound Request
       │
       ▼
TenantResolutionFilter
  Extract X-Tenant-ID header
  Validate API key against tenants table
  Store TenantContext in reactive context
       │
       ▼
PolicyEnforcementFilter (pre-routing)
  Load active policy for tenant
  Evaluate request against policy rules (SpEL)
  If BLOCK rule triggered AND confidence ≥ 0.8: reject (403)
  Otherwise: proceed
       │
       ▼
RateLimitFilter
  Redis sliding window token bucket: (tenantId, endpointId)
  Limits from active policy rate_limit.requests_per_minute
       │
       ▼
Spring Cloud Gateway Route
  DynamicRouteLocator resolves target URL from endpoints table
       │
       ▼
Downstream API
       │
       ▼
CallCaptureService
  Record request + response bodies, latency, status
  Build ApiCall aggregate
  Write to api_calls + outbox_events in one transaction
       │
       ▼
Outbox Relay → Kafka: aether.api.calls
       │
       ├──► MemoryService (aether-memory module)
       │    Embed call context → store in memory_embeddings
       │
       └──► AgentOrchestrator (aether-agents module)
            Route to GovernanceAgent, RetryAgent, etc.
```

---

## Memory Lifecycle

```
ApiCallRecordedEvent arrives via Kafka
       │
       ▼
MemoryService.store(call)
  Build text representation of the call
       │
       ▼
EmbeddingService.embed(text)
  POST /api/embeddings → Ollama (all-MiniLM-L6-v2)
  Returns float[384]
       │
       ▼
PgVectorMemoryStore.store(record)
  INSERT INTO memory_embeddings (content, embedding, memory_type, ...)
       │
Later: Agent lookup
       │
       ▼
MemoryService.findSimilarCalls(context, topK=10)
  EmbeddingService.embed(context) → query vector
       │
       ▼
PgVectorMemoryStore.findSimilar(queryVector, topK)
  SELECT ... FROM memory_embeddings
  ORDER BY embedding <-> :queryVector  -- cosine distance
  LIMIT :topK
       │
       ▼
Returns List<MemoryRecord> to calling agent
  Agent builds prompt: context + top-K memories + active policy
```

### Memory Compaction (Monthly)
The `MemoryCompactionJob` runs on the 1st of each month at 02:00 UTC:
1. Fetch all `MemoryRecord`s older than 30 days where `strength < 0.3`
2. Summarize in batches via the LLM: "Summarize these N API interaction memories into key patterns"
3. Store one `SEMANTIC` summary record
4. Delete the originals in a transaction

---

## Agent Lifecycle

```
AgentInput created (ApiCall + relevant memories + active policy)
       │
       ▼
AgentOrchestrator.orchestrate(input)
  Build OrchestrationPlan: which AgentCapabilities are needed
  (based on call outcome, policy status, confidence thresholds)
       │
       ▼
For each capability in plan:
  AgentRegistry.findByCapability(capability) → List<Agent>
  Agent.process(input) → AgentOutput
  If AgentOutput.confidence < 0.8: DO NOT auto-block (human-in-the-loop)
  Persist AgentDecision to agent_decisions table
  Publish AgentDecisionEvent to Kafka
       │
       ▼
OrchestrationResult returned
  Contains all AgentOutputs, aggregated confidence, recommended action
```

---

## Security

| Concern | Implementation |
|---|---|
| Authentication (services) | `X-API-Key` header — SHA-256 hash compared against `tenants.api_key_hash` |
| Authentication (humans) | JWT via Spring Security 6 OAuth2 Resource Server |
| Multi-tenant data isolation | All queries include `WHERE tenant_id = :tenantId`; PG row-level security on sensitive tables |
| Secrets management | Environment variables (local); AWS Secrets Manager (production). Never committed to source. |
| GDPR | `GdprRedactionService` strips PII (email, phone, card) before any persistence |
| Audit | `audit_log` table — immutable, append-only |
| Transport | HTTPS enforced in production (K8s ingress TLS termination) |
| LLM prompt injection | Input sanitization before building agent prompts; model outputs validated by HallucinationDetectorAgent |

---

## Observability

### Metrics (Micrometer → Prometheus)
```
aether.proxy.calls.total{tenant, endpoint, outcome}
aether.proxy.latency.ms{tenant, endpoint}          -- histogram
aether.memory.embeddings.total{tenant, type}
aether.memory.similarity.queries.total{tenant}
aether.agents.decisions.total{agent, decision}
aether.agents.confidence{agent}                    -- histogram
aether.policy.evaluations.total{tenant, result}
aether.policy.violations.total{tenant, rule}
aether.circuit.breaker.state{tenant, endpoint}
```

### Distributed Tracing (OpenTelemetry → Grafana Tempo)
Every inbound proxy request gets a trace. Spans cover:
- `proxy.filter.tenant-resolution`
- `proxy.filter.policy-enforcement`
- `proxy.filter.rate-limit`
- `memory.embed`
- `memory.store`
- `agent.orchestrate`
- `agent.{agent-name}.process`

Trace IDs propagated to downstream APIs via `traceparent` header (W3C Trace Context).

---

## EEIK Bootstrap Integration

AetherGrid's development environment is governed by [eeik-bootstrap](https://github.com/suplab/eeik-bootstrap):

- **44 Claude Code agents** in `.claude/agents/` (java-developer, architect, ai-engineer, ai-governance-officer, security-auditor, dba-advisor, kubernetes-engineer, ci-engineer, etc.)
- **19 slash commands**: `/estimate`, `/review`, `/adr`, `/security-scan`, `/deploy-check`, `/coverage-report`, `/memory-update`, etc.
- **Persistent memory**: `.claude/memory/` with 7 files seeded with AetherGrid-specific context
- **Safety hooks**: pre-write guard (blocks dangerous paths), pre-bash guard (blocks destructive git/SQL), post-edit check (warns on `@Autowired`, `javax.*`, `SELECT *`)
- **10 golden rules** enforced at code review: constructor injection, no hardcoded secrets, SLF4J, explicit SQL columns, parameterized queries, `jakarta.*`, Conventional Commits, no TODOs, SOLID, DDD

---

*Next: [Roadmap](roadmap.md) · [Progress](progress.md)*
