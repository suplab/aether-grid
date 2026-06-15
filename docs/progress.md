# Aether — Development Progress

> This file is updated with every commit. It is the live source of truth for what has been built.

---

## Current Status

**Active Phase:** Phase 12 — CI/CD + Kubernetes
**Branch:** `claude/enterprise-app-planning-setup-whtxmu`
**Last Updated:** 2026-06-15

---

## Phase Summary

| Phase | Name | Status | Commits |
|---|---|---|---|
| 0 | Concept & Documentation | ✅ Complete | 1 |
| 1 | EEIK Bootstrap Integration | ✅ Complete | 1 |
| 2 | Maven Multi-Module Foundation | ✅ Complete | 1 |
| 3 | Infrastructure Stack | ✅ Complete | 1 |
| 4 | Core Domain Model + LLM Abstraction | ✅ Complete | 1 |
| 5 | Proxy Layer | ✅ Complete | 1 |
| 6 | Memory Layer | ✅ Complete | 1 |
| 7 | Agent Subsystem | ✅ Complete | 1 |
| 8 | Policy Engine | ✅ Complete | 1 |
| 9 | Admin REST API + Observability | ✅ Complete | 1 |
| 10 | Advanced Agents + Observability | ✅ Complete | 1 |
| 11 | Multi-Tenancy + Compliance | ✅ Complete | 1 |
| 12 | CI/CD + Kubernetes | 📋 Planned | — |

---

## Phase 0 — Complete ✅

**Commit:** `docs: establish aether ecosystem concept, architecture, and project roadmap`

### What was done

- `README.md` — complete rewrite incorporating full Aether ecosystem vision (three layers: Aether philosophy, Aether Core personal cognitive engine, AetherGrid distributed intelligence). Enterprise architecture diagram, agent table, tech stack, use cases, risks & mitigations, quick start, documentation index.

- `docs/index.html` — self-contained visual concept page with dark enterprise theme. Sections: three-layer ecosystem overview, four core principles, Aether Core component flow, AetherGrid architecture diagram, all six agent cards, technology stack table, phased roadmap. eeik-bootstrap promo in footer.

- `docs/architecture.md` — technical deep-dive: module dependency graph, hexagonal architecture pattern, domain event sealed hierarchy, agent plugin SPI, transactional outbox, policy-as-code (SpEL in YAML), full data model (9 tables), API proxy flow, memory lifecycle, agent lifecycle, security model, observability metrics and tracing.

- `docs/roadmap.md` — phased delivery plan for all 12 phases with deliverables, commit messages, and verification steps.

- `docs/progress.md` — this file. Live development tracker.

### Files created/modified

| File | Change |
|---|---|
| `README.md` | Rewritten — full Aether ecosystem, architecture, agents, tech stack |
| `docs/index.html` | Created — visual concept page |
| `docs/architecture.md` | Created — technical architecture |
| `docs/roadmap.md` | Created — phased delivery roadmap |
| `docs/progress.md` | Created — this file |

---

## Phase 1 — EEIK Bootstrap Integration ✅

**Commit:** `chore(bootstrap): integrate eeik governance layer — CLAUDE.md, memory, hooks, commands`

### What was done

- `CLAUDE.md` — full project brief: Aether ecosystem description, tech stack, golden rules, slash commands, memory file index, prohibited patterns, docs sync rule
- `.claude/memory/project-context.md` — service inventory (7 services/modules), Kafka topics, auth methods, local URLs, env var names, DB schema
- `.claude/memory/domain-glossary.md` — 33 Aether-specific terms defined
- `.claude/memory/decisions.md` — 8 key decisions recorded (D-001 through D-008)
- `.claude/memory/constraints.md` — 10 EEIK golden rules + 8 Aether-specific hard constraints
- `.claude/memory/patterns.md` — 8 approved patterns with code examples
- `.claude/memory/tech-debt.md` — initialized (empty)
- `.claude/memory/session-log.md` — seeded with Phase 0 and Phase 1 entries
- `.claude/hooks/` — 4 safety hooks (pre-bash-guard, pre-write-guard, post-edit-check, on-stop)
- `.claude/commands/` — 5 slash commands (/estimate, /review, /adr, /memory-update, /security-scan)
- `aether.manifest.yaml` — EEIK project manifest (agent-platform, multi-agent AI, GDPR compliance)

---

## Phase 2 — Maven Multi-Module Foundation ✅

**Commit:** `build(infra): add parent POM and 7-module Maven structure`

### What was done

- `pom.xml` (parent) — Spring Boot 3.3.5 BOM, Spring Cloud 2023.0.3 BOM, Resilience4j 2.2.0 BOM, Testcontainers BOM. Java 21 compiler with `--enable-preview`. Maven Enforcer requires Java 21+ and Maven 3.9+. JaCoCo 80% line coverage gate. All internal module versions managed centrally.
- `aether-core/pom.xml` — jakarta.validation-api, slf4j-api, assertj (test)
- `aether-memory/pom.xml` — spring-boot-starter, spring-data-jdbc, postgresql, pgvector, spring-kafka, testcontainers-postgresql/kafka
- `aether-agents/pom.xml` — aether-core + aether-memory, spring-boot-starter, spring-kafka, spring-web (RestClient for Ollama), jackson-databind, mockito
- `aether-policy/pom.xml` — aether-core, spring-boot-starter, spring-data-jdbc, postgresql, spring-kafka, jackson-dataformat-yaml, flyway-core, flyway-database-postgresql
- `aether-proxy/pom.xml` — aether-core + aether-policy, spring-cloud-starter-gateway, circuit-breaker-reactor-resilience4j, spring-data-redis-reactive, spring-kafka, spring-data-jdbc, actuator, micrometer-prometheus, reactor-test
- `aether-api/pom.xml` — all library modules, spring-boot-starter-web, spring-security, oauth2-resource-server, spring-data-jdbc, spring-validation, actuator, micrometer-prometheus, micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp, springdoc-openapi 2.6.0, spring-security-test
- `aether-infra/pom.xml` — pom packaging only (no Java source)
- `aether-proxy/src/main/java/.../AetherProxyApplication.java` — Spring Boot entry point (port 8080)
- `aether-proxy/src/main/resources/application.yml` — Gateway, datasource, Redis, Kafka, actuator config
- `aether-api/src/main/java/.../AetherApiApplication.java` — Spring Boot entry point (port 8081)
- `aether-api/src/main/resources/application.yml` — datasource, Kafka, security, springdoc, OTel config

### Verification result
`mvn validate` — passed (no output = clean)

---

## Phase 3 — Infrastructure Stack ✅

**Commit:** `infra: add docker compose stack and flyway migrations`

### What was done

- `aether-infra/docker/docker-compose.yml` — 7 services with `condition: service_healthy` dependency chains: pgvector/pgvector:pg16, redis:7-alpine, confluentinc/cp-zookeeper:7.7.1, confluentinc/cp-kafka:7.7.1, ollama/ollama:latest, prom/prometheus:v2.55.0, grafana/grafana:11.3.0
- `aether-infra/docker/prometheus.yml` — scrapes host.docker.internal:8080 and :8081 actuator endpoints
- `aether-infra/docker/.env.example` — documents required env var names with no values
- `aether-infra/db/migration/V001__create_tenants.sql` through `V009__create_outbox_events.sql` — full schema: tenants, endpoints, api_calls, memory_embeddings (pgvector 384-dim IVFFlat index), policies, policy_versions, agent_decisions, audit_log (no FK by design), outbox_events (partial index on unread rows)

### Verification result
`docker compose config --quiet` — valid stack. All migrations forward-only with no destructive DDL.

---

## Phase 4 — Core Domain Model + LLM Abstraction ✅

**Commit:** `feat(core): implement domain model, events, ports, and multi-provider LLM abstraction`

### What was done

**`aether-core` — pure domain (no Spring dependency):**
- Value objects (Java 21 records): `ApiCallId`, `TenantId`, `ApiEndpoint`, `CallMetrics`, `MemoryRecord`
- Enums: `CallOutcome`, `HttpMethod`, `MemoryType`, `TenantStatus`
- Aggregates: `ApiCall` (raises `ApiCallRecordedEvent` on creation, pulls events after dispatch), `Tenant` (lifecycle: ACTIVE → SUSPENDED / DEPROVISIONED)
- Sealed `DomainEvent` interface with four `permits`: `ApiCallRecordedEvent`, `PolicyViolatedEvent`, `AgentDecisionEvent`, `GovernanceUpdatedEvent` — exhaustive switch pattern matching enforced by compiler
- Port interfaces: `EventPublisher`, `ApiCallRepository`, `TenantRepository`, `MemoryStore`, `EmbeddingPort`, `PolicyRepository`
- Exception hierarchy: `AetherException` (abstract), `TenantNotFoundException`, `PolicyViolationException`, `AgentException`
- Unit tests (19 tests, all green): `ApiCallTest`, `TenantTest`, `CallMetricsTest`, `DomainEventTest`

**`aether-agents` — multi-provider LLM abstraction:**
- `LlmClient` interface — single `complete(LlmRequest) → LlmResponse` method + `provider()` + `isAvailable()`
- `LlmRequest` / `LlmResponse` records with full validation
- `LlmProvider` enum: `OLLAMA`, `GROQ`, `ANTHROPIC`
- `OllamaLlmClient` — local Ollama `/api/chat` (Gemma2:2b, Phi-3-mini, any local model)
- `GroqLlmClient` — Groq cloud inference (Llama-3.3-70b, Mixtral-8x7b, Gemma2-9b) via OpenAI-compatible API
- `AnthropicLlmClient` — Claude models (claude-haiku-4-5-20251001, claude-sonnet-4-6) via Anthropic Messages API
- `LlmClientConfig` — `@ConditionalOnProperty(name = "aether.llm.provider")` wires the correct adapter at startup; Ollama is the default
- `application-agents.yml` — all provider config with env var overrides (`AETHER_LLM_PROVIDER`, `GROQ_API_KEY`, `ANTHROPIC_API_KEY`, `OLLAMA_MODEL`, etc.)
- Agent SPI: `Agent` interface, `AgentCapability` enum, `AgentInput` / `AgentOutput` records, `AgentDecision` enum
- Confidence gate enforced in `AgentOutput` compact constructor: `BLOCK` with confidence < 0.8 → `autoEnforced = false` → human-in-the-loop
- `AgentRegistry` — Spring-injected `List<Agent>`, `disableAgent(type)` kill-switch, `findByCapability()`

### Verification result
`mvn test -pl aether-core` — 19 tests, 0 failures. `mvn compile -pl aether-agents -am` — clean build.

---

## Phase 5 — Proxy Layer ✅

**Commit:** `feat(proxy): implement gateway filters, outbox relay, and Redis rate limiting`

### What was done

- `TenantAuthFilter` (GlobalFilter, order=-100) — resolves `X-API-Key` header → SHA-256 hash → tenant lookup; returns 401 for unknown/suspended tenants; actuator paths bypass auth; stores `TenantContext` in exchange attributes
- `RedactionFilter` (GlobalFilter, order=-90) — strips sensitive headers (`Authorization`, `X-API-Key`, `Cookie`, `Set-Cookie`, `X-Client-Secret`) from the sanitised request forwarded downstream
- `ApiCallCaptureFilter` (GlobalFilter, order=-50) — uses `doFinally` hook to capture response status + latency after chain completes; serialises `ApiCallRecordedEvent` as JSON into `outbox_events` table via fire-and-forget on `boundedElastic` scheduler
- `JdbcTenantRepository` — `NamedParameterJdbcTemplate` implementation of `TenantRepository` port; `ON CONFLICT DO UPDATE` upsert
- `JdbcOutboxRepository` — writes to `outbox_events` with JSONB payload; `findUnpublished(limit)` uses partial index on `published = false`; `markPublished(ids)` bulk-updates with `ANY(:ids::uuid[])`
- `OutboxRelayScheduler` — `@Scheduled(fixedDelayString = "${aether.outbox.relay-interval-ms:5000}")` reads up to 100 unpublished events, publishes to Kafka topic via `KafkaTemplate.send(...).get()`, marks published in a batch
- `TenantKeyResolver` — `KeyResolver` bean; extracts tenant ID from exchange attributes for Redis rate limiter; falls back to IP for unauthenticated requests
- `ProxyConfig` — `@Configuration` wires all proxy beans (explicit constructor injection, no `@Autowired`)
- `application.yml` updated — `RequestRateLimiter` default filter (100 rps / 200 burst), `CircuitBreaker` on catch-all route with Resilience4j config (50% failure threshold, 30s open duration), Kafka producer set to `acks=all` + idempotent
- Unit tests: `TenantAuthFilterTest` — 5 tests covering missing key, unknown key, suspended tenant, valid tenant (context stored), actuator bypass

### Verification result
`mvn test -pl aether-proxy` — 5 tests, 0 failures.

---

## Phase 6 — Memory Layer ✅

**Commit:** `feat(memory): implement embedding service, pgvector store, lifecycle, and Kafka consumer`

### What was done

- `OllamaEmbeddingService` — implements `EmbeddingPort`; calls Ollama `/api/embed` (all-MiniLM-L6-v2, 384-dim); validates returned dimension; throws `EmbeddingException` on failure
- `PGVectorMemoryStore` — implements `MemoryStore` port with `NamedParameterJdbcTemplate`:
  - `store()` — `ON CONFLICT DO UPDATE` upsert; `float[]` ↔ `[x,y,z]` string conversion for `::vector` cast
  - `findSimilar()` — cosine distance via pgvector `<=>` operator; reinforces strength by 5% on access
  - `findByType()` — filtered retrieval sorted by strength desc
  - `delete()` — tenant-scoped hard delete
  - Vector string helpers: `toVectorString()` / `parseVectorString()` (round-trip safe)
- `MemoryLifecycleService` — two `@Scheduled` jobs:
  - Daily decay (3am): 5% strength reduction on records idle > 7 days; parameterized interval via `(:idleDays * INTERVAL '1 day')`
  - Weekly compaction (4am Sunday): purges records with strength < 0.05
- `ApiCallMemoryConsumer` — `@KafkaListener` on `aether.api.calls` topic; parses JSON payload; generates embedding via Ollama; classifies memory type (PROCEDURAL=success, SEMANTIC=4xx, EPISODIC=5xx/timeout); stores `MemoryRecord`; embedding failures skip gracefully without propagating
- `MemoryConfig` — `@Configuration` wiring with constructor injection
- `application-memory.yml` — Ollama config, topic names, decay/compaction cron via env vars
- `pom.xml` updated — added `spring-web` (RestClient) and `jackson-databind`
- Unit tests (9 tests, 0 failures): `PGVectorMemoryStoreTest` (round-trip serialization, 384-dim, null safety), `ApiCallMemoryConsumerTest` (memory type classification, embedding failure skip, malformed payload safety)

### Verification result
`mvn test -pl aether-memory` — 9 tests, 0 failures.

---

## Phase 7 — Agent Subsystem ✅

**Commit:** `feat(agents): implement orchestrator, governance, retry, and hallucination detector agents`

### What was done

- `AgentOrchestrator` — dispatches `AgentInput` to all registered agents matching the capability; enforces `MAX_ITERATIONS=5` guard; `orchestrateParallel()` runs independent inputs concurrently via `VirtualThreadPerTaskExecutor` + `CompletableFuture.allOf()`
- `OrchestrationResult` record — aggregates all outputs; `requiresHumanReview()` (BLOCK + not auto-enforced), `hasAutoBlock()` (BLOCK + auto-enforced), `highestSeverityDecision()` (severity ranking: BLOCK > ALERT > SUGGEST > DEFER > ALLOW)
- `GovernanceAgent` — queries LLM with JSON response protocol; parses `decision`/`confidence`/`rationale` from response; defaults to ALLOW on LLM failure or parse error; respects confidence gate
- `RetryAgent` — counts failure/timeout memories from context; skips LLM for zero-failure calls (fast path); suggests exponential backoff on LLM failure
- `HallucinationDetectorAgent` — validates LLM-generated governance rules against memory patterns; defaults to ALERT when LLM unavailable; requires >= 1 memory record for meaningful detection
- `AgentsConfig` — `@Configuration` wiring all agent beans + registry + orchestrator via constructor injection
- Unit tests (14 tests, 0 failures): `AgentOrchestratorTest` (dispatch, no-agent, human review, auto-block), `AgentRegistryTest` (capability lookup, kill-switch, enable/disable), `GovernanceAgentTest` (ALLOW/BLOCK parse, confidence gate, LLM failure fallback)

### Verification result
`mvn test -pl aether-agents` — 14 tests, 0 failures.

---

## Phase 8 — Policy Engine ✅

**Commit:** `feat(policy): implement SpEL policy engine, GDPR redaction, audit log, and policy store`

### What was done

- `PolicyRule` record — name, condition (SpEL expression), `PolicyAction` enum, priority
- `PolicyEvaluationContext` record — method, path, responseCode, latencyMs, outcome, tenantId, headers
- `PolicyEvaluationResult` record — `overallAction`, matched `RuleMatch` list, `isBlocked()`, `hasAlerts()`
- `SpelPolicyEngine` — loads active policy YAML per tenant from DB; parses `rules[]` array; evaluates SpEL conditions via `SimpleEvaluationContext` (read-only, prevents arbitrary execution); sorts rules by priority DESC; determines overall action by severity (BLOCK > RATE_LIMIT > ALERT > AUDIT > ALLOW); gracefully skips malformed rule expressions
- `JdbcPolicyRepository` — implements `PolicyRepository` port; `ON CONFLICT DO UPDATE` upsert; auto-versions via `MAX(version) + 1`; `activatePolicy()` supersedes previous active policy first (enforcing single-active-per-tenant invariant)
- `GdprRedactionService` — regex-based PII detection and redaction for: email, E.164 phone, Visa/MC/Amex credit cards, SSN, JWT Bearer tokens, API keys; `containsPii()` for pre-check; `redact()` replaces all matches with `[REDACTED]`
- `AuditLogService` — `NamedParameterJdbcTemplate` insert to `audit_log` table with JSONB detail payload; no FK constraints by design (survives entity deletion)
- `PolicyConfig` — `@Configuration` wiring all beans
- Unit tests (14 tests, 0 failures): `SpelPolicyEngineTest` (no-policy allow, latency block, error alert, no-match allow, block-over-alert priority, malformed YAML safety), `GdprRedactionServiceTest` (email, credit card, SSN, JWT, clean passthrough, null safety, multi-PII)

### Verification result
`mvn test -pl aether-policy` — 14 tests, 0 failures.

---

## Phase 9 — Admin REST API + Observability ✅

**Commit:** `feat(api): implement admin REST API — tenant, policy, memory controllers`

### What was done

- `TenantController` — `@RestController @RequestMapping("/api/v1/tenants")`; `onboard` (POST) with SHA-256 API key hashing before storage; `get` (GET /{tenantId}); `suspend` (PUT /{id}/suspend); `reactivate` (PUT /{id}/reactivate); all lifecycle transitions respect `TenantNotFoundException`
- `PolicyController` — `@RequestMapping("/api/v1/tenants/{tenantId}/policies")`; `create` (POST) generates UUID policyId and saves as DRAFT; `activate` (PUT /{policyId}/activate); `archive` (PUT /{policyId}/archive); `getActive` (GET /active) returns YAML or 404
- `MemoryController` — `@RequestMapping("/api/v1/tenants/{tenantId}/memory")`; `search` (POST /search) embeds query via `EmbeddingPort`, retrieves similar records via `MemoryStore`; `delete` (DELETE /{memoryId}) tenant-scoped hard delete
- `GlobalExceptionHandler` — `@RestControllerAdvice`; RFC 7807 `ProblemDetail` responses for `TenantNotFoundException` (404), `AetherException` (500), `MethodArgumentNotValidException` (400 + field errors map), `IllegalArgumentException` (400)
- `SecurityConfig` — `@EnableWebSecurity`; stateless JWT OAuth2 resource server; actuator + Swagger UI permitted without auth; all `/api/**` requires authentication; CSRF disabled
- `ApiConfig` — `@Configuration`; wires `TenantRepository` adapter (`JdbcApiTenantRepository` private inner class — UPSERT on `tenants` table)
- `AetherApiApplication` updated — `scanBasePackages = "com.suplab.aether"` to pick up `MemoryConfig`, `PolicyConfig`, `AgentsConfig` from dependent modules
- `pom.xml` (parent) — added `-parameters` compiler flag for Spring MVC path variable name resolution
- MockMvc slice tests (17 tests, 0 failures): `TenantControllerTest` (7 tests), `PolicyControllerTest` (6 tests), `MemoryControllerTest` (4 tests)

### Files created/modified

| File | Change |
|---|---|
| `aether-api/.../controller/TenantController.java` | Created |
| `aether-api/.../controller/PolicyController.java` | Created |
| `aether-api/.../controller/MemoryController.java` | Created |
| `aether-api/.../controller/GlobalExceptionHandler.java` | Created |
| `aether-api/.../security/SecurityConfig.java` | Created |
| `aether-api/.../config/ApiConfig.java` | Created |
| `aether-api/.../AetherApiApplication.java` | Updated — scanBasePackages |
| `aether-api/src/test/.../TenantControllerTest.java` | Created |
| `aether-api/src/test/.../PolicyControllerTest.java` | Created |
| `aether-api/src/test/.../MemoryControllerTest.java` | Created |
| `pom.xml` | Added `-parameters` compiler flag |

### Verification result
`mvn clean test -pl aether-api` — 17 tests, 0 failures.

---

## Phase 10 — Advanced Agents + Observability ✅

**Commit:** `feat(agents): add temporal prediction, reflection agents, and Micrometer metrics`

### What was done

- `TemporalPredictionAgent` — analyses EPISODIC (failures/timeouts) and SEMANTIC (4xx) memory counts; fast-path DEFER for zero memories (confidence=0.3) or zero episodic+semantic (confidence=0.5); queries LLM for `ALERT|DEFER` prediction with JSON response protocol; graceful fallback on LLM failure
- `ReflectionAgent` — computes health score as `proceduralCount / (total + 1)`; fast-path ALLOW (no LLM) when health ≥ 0.5, reporting score as confidence; queries LLM for `SUGGEST|DEFER` when health is poor; graceful fallback on LLM failure or no memories
- `AgentOrchestrator` updated — `MeterRegistry` constructor parameter; counter `aether.agent.executions` (tags: agent, decision) incremented per execution; timer `aether.agent.latency` (tag: agent) records per-agent latency in milliseconds
- `AgentsConfig` updated — wires `TemporalPredictionAgent`, `ReflectionAgent` beans; passes `MeterRegistry` to `AgentOrchestrator`
- `AgentCapability` enum — `TEMPORAL_PREDICTION` and `REFLECTION` already present from Phase 4
- `aether-agents/pom.xml` — added `micrometer-core` compile dependency and `micrometer-test` test dependency
- Unit tests (26 total, 0 failures): `TemporalPredictionAgentTest` (6 tests), `ReflectionAgentTest` (6 tests), all prior tests continue passing

### Files created/modified

| File | Change |
|---|---|
| `aether-agents/.../temporal/TemporalPredictionAgent.java` | Created |
| `aether-agents/.../reflection/ReflectionAgent.java` | Created |
| `aether-agents/.../orchestrator/AgentOrchestrator.java` | Updated — MeterRegistry + metrics |
| `aether-agents/.../config/AgentsConfig.java` | Updated — new agent beans |
| `aether-agents/.../pom.xml` | Updated — micrometer-core + micrometer-test |
| `aether-agents/src/test/.../TemporalPredictionAgentTest.java` | Created |
| `aether-agents/src/test/.../ReflectionAgentTest.java` | Created |
| `aether-agents/src/test/.../AgentOrchestratorTest.java` | Updated — SimpleMeterRegistry |

### Verification result
`mvn clean test -pl aether-agents` — 26 tests, 0 failures.

---

## Phase 11 — Multi-Tenancy + Compliance ✅

**Commit:** `feat(compliance): GDPR memory opt-out, right-to-erasure, RLS, and audit logging`

### What was done

- `V010__tenant_gdpr_preferences.sql` — `memory_opt_out BOOLEAN NOT NULL DEFAULT FALSE` and `data_retention_days INT NOT NULL DEFAULT 365` added to `tenants` table
- `V011__row_level_security.sql` — PostgreSQL RLS enabled on `memory_embeddings`, `api_calls`, `policies`, `agent_decisions`, `audit_log`; `FORCE ROW LEVEL SECURITY` on `tenants`; all policies use `current_setting('app.tenant_id', true)` as the row filter
- `Tenant` domain — `memoryOptOut` field, `optOutOfMemory()`, `optIntoMemory()`, `reconstitute()` 5-arg overload (all already present from prior phase)
- `MemoryStore` port — `deleteAll(TenantId)` (already present from prior phase)
- `PGVectorMemoryStore` — `deleteAll()` implemented with tenant-scoped `DELETE` (already present)
- `ApiCallMemoryConsumer` — tenant opt-out check (skip if `memoryOptOut=true` or tenant not found) and GDPR redaction before embedding (already wired)
- `JdbcTenantRepository` (proxy) — `memory_opt_out` added to SELECT and UPSERT SQL; `TENANT_MAPPER` calls 5-arg `reconstitute()`
- `AuditLogService` — `log(TenantId, action, detail, actor)` convenience overload; `findByTenant(TenantId, int limit)` query returning `List<Map<String, Object>>`; SQL corrected to use actual schema column names (`occurred_at`, not `created_at`)
- `TenantResponse` record — `memoryOptOut` field added; `from()` maps `tenant.memoryOptOut()`
- `TenantController` — `AuditLogService` and `MemoryStore` added as constructor parameters; `PUT /{id}/gdpr/memory-opt-out` (opt out), `DELETE /{id}/gdpr/memory-opt-out` (opt in), `DELETE /{id}/memories` (right-to-erasure); audit events logged for all lifecycle transitions
- `PolicyController` — `AuditLogService` added as constructor parameter; `POLICY_CREATED`, `POLICY_ACTIVATED`, `POLICY_ARCHIVED` audit events
- `AuditController` — `GET /api/v1/tenants/{tenantId}/audit?limit=50` — returns paged audit log for a tenant
- Tests: `ApiCallMemoryConsumerTest` (12 tests — added opt-out skip, unknown tenant skip, redaction verification); `TenantControllerTest` (11 tests — added opt-out, opt-in, erasure, 404-on-erasure); `AuditControllerTest` (3 tests — list, empty, limit param); `PolicyControllerTest` updated with `AuditLogService` mock bean

### Verification result

`mvn clean test -pl aether-memory` — 12 tests, 0 failures  
`mvn clean test -pl aether-api` — 24 tests, 0 failures  
`mvn clean test -pl aether-proxy` — 5 tests, 0 failures  
`mvn clean test -pl aether-policy` — 14 tests, 0 failures

### Files created/modified

| File | Change |
|---|---|
| `aether-infra/db/migration/V010__tenant_gdpr_preferences.sql` | Created — `memory_opt_out`, `data_retention_days` columns |
| `aether-infra/db/migration/V011__row_level_security.sql` | Created — RLS policies on all tenant-scoped tables |
| `aether-proxy/.../repository/JdbcTenantRepository.java` | Updated — `memory_opt_out` in SQL + 5-arg `reconstitute()` |
| `aether-policy/.../audit/AuditLogService.java` | Updated — `log(TenantId,…)` overload + `findByTenant()` |
| `aether-api/.../dto/TenantResponse.java` | Updated — `memoryOptOut` field |
| `aether-api/.../controller/TenantController.java` | Updated — GDPR endpoints + audit logging |
| `aether-api/.../controller/PolicyController.java` | Updated — `AuditLogService` + audit events |
| `aether-api/.../controller/AuditController.java` | Created — audit query endpoint |
| `aether-memory/.../consumer/ApiCallMemoryConsumerTest.java` | Updated — 4-arg constructor + 3 new tests |
| `aether-api/src/test/.../TenantControllerTest.java` | Updated — `AuditLogService`/`MemoryStore` mocks + 4 new tests |
| `aether-api/src/test/.../PolicyControllerTest.java` | Updated — `AuditLogService` mock bean |
| `aether-api/src/test/.../AuditControllerTest.java` | Created — 3 tests |

---

## Phase 12 — CI/CD + Kubernetes 📋

_Not yet started._

---

## Commit Log

| Date | Phase | Commit | Description |
|---|---|---|---|
| 2026-06-14 | 0 | `docs: establish aether ecosystem concept, architecture, and project roadmap` | README rewrite, docs/index.html, architecture.md, roadmap.md, progress.md |

---

*See [Roadmap](roadmap.md) for planned deliverables · [Architecture](architecture.md) for technical detail*
