# Aether — Development Roadmap

> Living document. Updated with each phase completion. Last updated: Phase 0.

---

## Guiding Principles

- **Small chunks** — each phase is a logical unit committable in one or few commits
- **Docs in sync** — every commit that changes behavior also updates README, this file, index.html, and progress.md
- **Working software first** — infrastructure before features; domain model before adapters
- **EEIK-governed** — all development follows eeik-bootstrap golden rules (constructor injection, no secrets, SLF4J, explicit SQL columns, `jakarta.*`, Conventional Commits)

---

## Status Legend

| Symbol | Meaning |
|---|---|
| ✅ | Complete |
| 🔄 | In Progress |
| 📋 | Planned |
| ⏸ | Deferred |

---

## Phase 0 — Concept & Documentation ✅

**Goal:** Establish the full Aether vision, enterprise-grade documentation, and project structure.

| Deliverable | Status |
|---|---|
| README.md — full rewrite with 3-layer Aether ecosystem | ✅ |
| docs/index.html — visual concept page | ✅ |
| docs/architecture.md — technical deep-dive | ✅ |
| docs/roadmap.md — this file | ✅ |
| docs/progress.md — live tracker | ✅ |

**Commit:** `docs: establish aether ecosystem concept, architecture, and project roadmap`

---

## Phase 1 — EEIK Bootstrap Integration 🔄

**Goal:** Every Claude Code session has full enterprise AI governance context from the first prompt.

| Deliverable | Status |
|---|---|
| `CLAUDE.md` — AetherGrid project brief (from PROJECT-CLAUDE.md template) | 📋 |
| `.claude/memory/project-context.md` — service inventory, ports, auth, environments | 📋 |
| `.claude/memory/domain-glossary.md` — AetherGrid-specific terminology | 📋 |
| `.claude/memory/decisions.md` — key architectural decisions | 📋 |
| `.claude/memory/constraints.md` — hard constraints + golden rules | 📋 |
| `.claude/memory/patterns.md` — approved patterns (agent SPI, outbox, policy-as-code) | 📋 |
| `.claude/memory/tech-debt.md` — debt register (empty initially) | 📋 |
| `.claude/memory/session-log.md` — rolling session log | 📋 |
| `.claude/agents/` — 44 specialist agents from eeik-bootstrap | 📋 |
| `.claude/commands/` — 19 slash commands from eeik-bootstrap | 📋 |
| `.claude/hooks/` — pre-write, pre-bash, post-edit, on-stop hooks | 📋 |
| `aether.manifest.yaml` — EEIK project manifest | 📋 |

**Commits:**
```
chore(bootstrap): copy eeik .claude/ config layer (agents, commands, hooks)
chore(bootstrap): initialize CLAUDE.md for aethergrid
chore(bootstrap): populate .claude/memory/ with aethergrid context
chore(bootstrap): add project manifest
```

**Verification:** Open Claude Code in project root. Ask "What is this project?" — should answer with AetherGrid context from memory. Run `/estimate "implement GovernanceAgent"` — should produce P50/P80/P90 breakdown.

---

## Phase 2 — Maven Multi-Module Foundation 📋

**Goal:** `mvn validate` passes across all modules. Module boundaries enforce bounded contexts.

| Deliverable | Status |
|---|---|
| `pom.xml` — parent POM (Java 21, Spring Boot 3.3 BOM, Spring Cloud BOM, all dependency versions) | 📋 |
| `aether-core/pom.xml` — shared domain module | 📋 |
| `aether-proxy/pom.xml` — proxy module | 📋 |
| `aether-memory/pom.xml` — memory module | 📋 |
| `aether-agents/pom.xml` — agents module | 📋 |
| `aether-policy/pom.xml` — policy module | 📋 |
| `aether-api/pom.xml` — admin API module | 📋 |
| `aether-infra/pom.xml` — infra module (reactor only) | 📋 |
| Minimal main class skeletons for `aether-proxy` and `aether-api` | 📋 |

**Commits:**
```
build(infra): add parent POM with Java 21, Spring Boot 3.3 BOM, and plugin management
build(infra): add module POMs and application skeletons
```

**Verification:** `mvn validate` passes. `mvn compile -pl aether-core` succeeds.

---

## Phase 3 — Infrastructure Stack 📋

**Goal:** `docker compose up` starts all services healthy. Flyway runs clean.

| Deliverable | Status |
|---|---|
| `aether-infra/docker/docker-compose.yml` — PostgreSQL+pgvector, Redis, Kafka+Zookeeper, Ollama, Prometheus, Grafana | 📋 |
| `aether-infra/docker/.env.example` — all environment variable names (no values) | 📋 |
| `V001__create_tenants.sql` | 📋 |
| `V002__create_endpoints.sql` | 📋 |
| `V003__create_api_calls.sql` | 📋 |
| `V004__create_memory_embeddings.sql` — includes `vector(384)` column | 📋 |
| `V005__create_policies.sql` | 📋 |
| `V006__create_policy_versions.sql` | 📋 |
| `V007__create_agent_decisions.sql` | 📋 |
| `V008__create_audit_log.sql` | 📋 |
| `V009__create_outbox_events.sql` | 📋 |
| pgvector IVFFlat index on `memory_embeddings.embedding` | 📋 |
| PG Row-Level Security policies (placeholder, enabled in Phase 11) | 📋 |

**Commits:**
```
feat(infra): add docker compose stack with healthchecks for all services
feat(db): add flyway migrations V001-V009 with pgvector schema
```

**Verification:** `docker compose up -d` → `docker compose ps` → all services show `healthy`. `flyway migrate` runs without error.

---

## Phase 4 — Core Domain Model 📋

**Goal:** `aether-core` compiles and unit tests pass at ≥80% line coverage (JaCoCo).

| Deliverable | Status |
|---|---|
| `ApiCall` aggregate (immutable, raises domain events) | 📋 |
| `ApiCallId`, `TenantId`, `ApiEndpoint`, `CallMetrics`, `CallOutcome` — value objects as Java records | 📋 |
| `Tenant` aggregate | 📋 |
| `DomainEvent` sealed interface + all event records | 📋 |
| `MemoryStore`, `EventPublisher`, `ApiCallRepository`, `PolicyRepository` port interfaces | 📋 |
| `AetherException`, `TenantNotFoundException`, `PolicyViolationException` | 📋 |
| Domain unit tests (`ApiCallTest`, `TenantTest`) | 📋 |

**Commits:**
```
feat(core): add domain aggregates, value objects, and sealed event hierarchy
feat(core): add outbound port interfaces and exception hierarchy
test(core): add domain model unit tests
```

**Verification:** `mvn test -pl aether-core` — all green, JaCoCo ≥80%.

---

## Phase 5 — Proxy Layer 📋

**Goal:** Requests proxied through Spring Cloud Gateway are captured and published as Kafka events.

| Deliverable | Status |
|---|---|
| `AetherProxyApplication` + `application.yml` | 📋 |
| `TenantResolutionFilter` — extracts `X-Tenant-ID`, validates API key | 📋 |
| `ProxyFilter` — captures request/response bodies, delegates to `CallCaptureService` | 📋 |
| `CallCaptureService` — builds `ApiCall`, writes transactional outbox | 📋 |
| Kafka adapter implementing `EventPublisher` | 📋 |
| `RateLimitFilter` — Redis sliding-window token bucket per `(tenantId, endpointId)` | 📋 |
| `CircuitBreakerConfig` — Resilience4j, per-tenant registry | 📋 |
| `FallbackHandler` — RFC 7807 ProblemDetail on open circuit | 📋 |
| `DynamicRouteLocator` — routes from DB, refreshes on `GovernanceUpdatedEvent` | 📋 |
| `ProxyFilterTest` (unit) + `ProxyApplicationIT` (Testcontainers) | 📋 |

**Verification:** Call routed via proxy → appears in Kafka topic `aether.api.calls` → appears in `api_calls` table.

---

## Phase 6 — Memory Layer 📋

**Goal:** `MemoryService` stores and retrieves semantically similar calls via PGVector.

| Deliverable | Status |
|---|---|
| `EmbeddingService` interface | 📋 |
| `OllamaEmbeddingService` — calls Ollama `/api/embeddings` | 📋 |
| `PgVectorMemoryStore` — parameterized, explicit columns, cosine distance | 📋 |
| `MemoryService` — Kafka listener for `ApiCallRecordedEvent`, embeds, stores | 📋 |
| `MemoryCompactionJob` — `@Scheduled`, monthly, batch summarization via LLM | 📋 |
| Unit tests + `MemoryServiceIT` (Testcontainers + pgvector) | 📋 |

**Verification:** Trigger proxied call → retrieve similar calls from `MemoryService.findSimilarCalls()` → results returned ranked by cosine similarity.

---

## Phase 7 — Agent Subsystem 📋

**Goal:** AgentOrchestrator, AgentRegistry, GovernanceAgent, and RetryAgent operational.

| Deliverable | Status |
|---|---|
| `Agent` SPI interface + `AgentCapability` enum | 📋 |
| `AgentInput` / `AgentOutput` records | 📋 |
| `AgentRegistry` — Spring `List<Agent>` constructor injection | 📋 |
| `AgentOrchestrator` — builds `OrchestrationPlan`, dispatches, emits `AgentDecisionEvent` | 📋 |
| `LlmClient` interface + `OllamaLlmClient` adapter | 📋 |
| `LlmPromptBuilder` — context + memory + policy → structured prompt | 📋 |
| `GovernanceAgent` — confidence gate at 0.8, human-in-the-loop enforcement | 📋 |
| `RetryAgent` — generates `RetryPolicy` from failure pattern analysis | 📋 |
| Agent unit tests + orchestrator test | 📋 |

**Verification:** Inject a policy-violating call → `GovernanceAgent` detects it → `AgentDecisionEvent` published → `agent_decisions` table populated.

---

## Phase 8 — Policy Engine 📋

**Goal:** YAML policies stored versioned in PostgreSQL, evaluated via SpEL at runtime, full audit trail.

| Deliverable | Status |
|---|---|
| `Policy` aggregate + `PolicyRule`, `PolicyVersion`, `PolicyStatus` | 📋 |
| `PolicyEngine` — loads active policy, iterates rules through `RuleEvaluator` | 📋 |
| `SpelRuleEvaluator` — sandboxed `SpelExpressionParser` with allowlisted context | 📋 |
| `JdbcPolicyStore` — versioned YAML in `policies` + `policy_versions` tables | 📋 |
| `PolicyYamlSerializer` — YAML ↔ `Policy` object | 📋 |
| `GdprRedactionService` — regex + NER-confidence PII detection, redacts before persistence | 📋 |
| `AuditLog` entity + `AuditLogRepository` (append-only) | 📋 |
| Policy engine unit tests + `JdbcPolicyStoreIT` | 📋 |

**Verification:** Create YAML policy via DB seed. Fire matching API call. `PolicyEvaluationResult` shows violation. `audit_log` records the evaluation. PII in call data is redacted before storage.

---

## Phase 9 — Admin REST API + Observability 📋

**Goal:** Full REST API for tenant/policy management. Metrics visible in Grafana.

| Deliverable | Status |
|---|---|
| `AetherApiApplication` + `application.yml` | 📋 |
| `TenantController` — CRUD, onboarding workflow | 📋 |
| `PolicyController` — CRUD + `/activate`, `/versions` | 📋 |
| `MemoryController` — `GET /api/v1/memory/search` | 📋 |
| `MetricsController` — aggregated call statistics per tenant | 📋 |
| `ApiSecurityConfig` — Spring Security 6, JWT + API key dual auth | 📋 |
| `ObservabilityConfig` — OpenTelemetry OTLP exporter, Micrometer custom meters | 📋 |
| `openapi/aether-api.yaml` — OpenAPI 3.1 contract-first spec | 📋 |
| Prometheus scrape config + Grafana dashboard JSON | 📋 |
| Controller MockMvc tests + `AetherApiIT` | 📋 |

**Verification:** Grafana dashboards load at `http://localhost:3000`. `curl .../api/v1/tenants` returns tenant list. Swagger UI at `.../api/swagger-ui.html`.

---

## Phase 10 — Advanced Agents 📋

**Goal:** Hallucination detection, policy drift monitoring, and temporal prediction operational.

| Deliverable | Status |
|---|---|
| `HallucinationDetectorAgent` — cosine similarity + rule-based consistency | 📋 |
| `PolicyDriftAgent` — rolling 24h window, KL divergence approximation | 📋 |
| `TemporalPredictionAgent` — time-series analysis, `FailureWindow` predictions in Redis | 📋 |
| `ReflectionAgent` — periodic system health evaluation | 📋 |
| Advanced agent unit tests | 📋 |

**Verification:** Inject artificial failure time-series → `TemporalPredictionAgent` predicts a failure window → `RetryAgent` reads prediction and adjusts policy.

---

## Phase 11 — Multi-Tenancy + Compliance 📋

**Goal:** Full tenant data isolation. GDPR-compliant data subject access and erasure.

| Deliverable | Status |
|---|---|
| PG Row-Level Security policies enabled (V010 migration) | 📋 |
| Spring Security integration sets PG session variable `app.current_tenant_id` | 📋 |
| `DataLineageRecord` entity — tracks all transformations per call | 📋 |
| `TenantOnboardingService` — full onboarding workflow | 📋 |
| `GdprController` — `GET /gdpr/export/{tenantId}`, `DELETE /gdpr/erase/{tenantId}` | 📋 |
| Tenant isolation integration test (Tenant A cannot see Tenant B data) | 📋 |
| GDPR erasure test (personal data + embeddings removed, audit log preserved) | 📋 |

---

## Phase 12 — CI/CD + Kubernetes 📋

**Goal:** Every push triggers a full quality gate. `helm install aether-grid` deploys to K8s.

| Deliverable | Status |
|---|---|
| `.github/workflows/ci.yml` — build → unit test (JaCoCo 80%) → integration test → OWASP + gitleaks | 📋 |
| `.github/workflows/release.yml` — multi-stage Docker builds, GHCR push via OIDC | 📋 |
| K8s manifests — Namespace, ConfigMap, Deployment, Service, Ingress, HPA | 📋 |
| Helm chart `helm/aether-grid/` — single command full-stack deploy | 📋 |
| `docs/adr/001-spring-cloud-gateway.md` through `006-flyway.md` | 📋 |

**Verification:** Open PR → all CI checks green → merge to `main` → release workflow pushes image → `helm install` → all pods `Running`.

---

## Future Considerations

These are tracked but not scoped for the current roadmap:

- **Self-improving agents** — agents that learn from feedback and update their own logic
- **Dynamic agent creation** — spawning new agents at runtime for novel task types
- **Agent marketplace** — catalog of shareable, versioned agent capabilities
- **Federated memory** — memory shared across organizational boundaries with consent controls
- **Edge intelligence** — lightweight agent runtime for IoT / embedded devices
- **Aether Core integration** — AetherGrid as the data plane backing a personal Aether Core instance
- **Web dashboard** — real-time governance and memory visualization UI

---

*Last updated: Phase 0 — Concept & Documentation*
*See [Progress](progress.md) for live status · [Architecture](architecture.md) for technical detail*
