# Aether

> A human-centric cognitive computing framework — philosophy, personal intelligence, and distributed autonomous agents.

**Author:** Suplab &nbsp;|&nbsp; **Status:** Active Development &nbsp;|&nbsp; **Phases Complete:** 0–10 of 12

---

## The Three Layers

```
Aether                          ← This repository
│
├── Philosophy                  Philosophy: the invisible cognitive fabric
│   connecting humans, memories, emotions, knowledge, and intelligence
│
├── Aether Core                 Personal cognitive engine — the individual mind OS
│   ├── Sensory Interface       Text, voice, documents, email, calendar, IoT sensors
│   ├── Context Engine          Current task · location · time · goals · history
│   ├── Memory Engine           Episodic · Semantic · Procedural · Emotional
│   ├── Knowledge Graph         Relationships as first-class citizens
│   ├── Reasoning Engine        System 1 (fast) · System 2 (deep) · Meta-reasoning
│   ├── Emotional Context       Stress · confidence · motivation · frustration
│   ├── Reflection Engine       What have I learned? What should be forgotten?
│   └── Action Engine           Reminders · workflows · APIs · agent coordination
│
└── Aether Grid                 Distributed agent ecosystem — collective intelligence
    ├── Multi-Agent Orchestration
    ├── Shared Semantic Memory (PGVector)
    ├── API Governance & Policy-as-Code
    ├── Event-Driven Communication (Kafka)
    └── Autonomous Self-Debugging
```

| Layer | Purpose |
|---|---|
| **Aether** | Philosophy and vision — the cognitive fabric |
| **Aether Core** | Individual cognitive engine — a personal mind OS |
| **Aether Grid** | Distributed agent ecosystem — collective intelligence |

---

## What is Aether?

Historically, _aether_ was the invisible medium believed to connect everything in the universe.

Here, **Aether** is the invisible cognitive fabric that connects humans, memories, emotions, knowledge, and intelligent systems. It is not merely an AI assistant — it is envisioned as:

- A **second mind** — a persistent reasoning companion
- A **memory extension** — cumulative knowledge across all interactions
- A **digital consciousness interface** — bridging human cognition and machine intelligence
- A **cognitive infrastructure** — the backbone for augmenting human capability, not replacing it

> _«Aether is a human-centric cognitive ecosystem. Aether Core is the digital extension of an individual's mind. Aether Grid is the distributed network of intelligent agents that transforms individual cognition into collective intelligence.»_

---

## Aether Grid — Current Implementation

Aether Grid is the distributed intelligence layer implemented in this repository (Phases 0–10 complete). It sits as a **smart proxy and governance layer** in front of any API ecosystem:

- **Remembers** every API interaction semantically (all-MiniLM-L6-v2 embeddings + metadata in pgvector, 384-dim)
- **Learns** patterns of successful and failing requests over time (PROCEDURAL/SEMANTIC/EPISODIC/EMOTIONAL memory types)
- **Governs** API usage via YAML policies stored in PostgreSQL and evaluated at runtime with SpEL
- **Predicts** temporal failure and latency windows with the `TemporalPredictionAgent`
- **Debugs itself**: `HallucinationDetectorAgent` validates LLM outputs, `ReflectionAgent` evaluates system health
- **Coordinates** five specialized agents across a cognitive mesh with shared pgvector memory
- **Protects PII** via `GdprRedactionService` (email, phone, cards, SSN, JWT, API keys) before any persistence
- **Enforces confidence gates**: agents with confidence < 0.8 on BLOCK decisions require human-in-the-loop

---

## Core Principles

### Persistent Memory
Traditional AI forgets. Aether remembers.
```
Experience → Understand → Remember → Associate → Evolve
```

### Emotional Intelligence
Human decisions are rarely purely rational. Aether models stress, confidence, motivation, and frustration — not to imitate them, but to understand and adapt to them.

### Contextual Reasoning
```
Person + Time + Environment + History + Emotional State = Meaning
```

### Human Augmentation
Agents with confidence < 0.8 never auto-block — human-in-the-loop is a hard constraint, not an afterthought. Aether amplifies human capability; it does not replace it.

---

## Repository Structure

```
aether/
├── .claude/
│   ├── agents/                    # 19 specialist agents (eeik-bootstrap)
│   ├── commands/                  # 5 slash commands (/estimate, /review, /adr, /memory-update, /security-scan)
│   ├── hooks/                     # Pre-write, pre-bash, post-edit, on-stop hooks
│   └── memory/
│       ├── project-context.md     # Service inventory, ports, environments
│       ├── domain-glossary.md     # Aether-specific terminology
│       ├── decisions.md           # Architecture Decision Records log
│       ├── constraints.md         # Hard constraints + golden rules
│       ├── patterns.md            # Approved patterns (agent SPI, outbox, policy-as-code)
│       ├── tech-debt.md           # Known debt tracker
│       └── session-log.md         # Rolling session log
├── .github/
│   ├── instructions/              # Copilot glob-based instructions
│   └── workflows/
│       ├── ci.yml                 # Build + test + quality gate
│       └── release.yml            # Container build + push (OIDC)
├── docs/
│   ├── index.html                 # Visual concept page (always in sync)
│   ├── architecture.md            # Technical architecture deep-dive
│   ├── roadmap.md                 # Phased delivery plan
│   ├── progress.md                # Live progress tracker
│   └── adr/                       # Architecture Decision Records
│       ├── 001-spring-cloud-gateway.md
│       ├── 002-pgvector-over-chroma.md
│       ├── 003-pluggable-agent-spi.md
│       ├── 004-kafka-event-bus.md
│       ├── 005-spel-policy-rules.md
│       └── 006-flyway-migrations.md
│
├── aether-core/                   # Shared domain models, events, port interfaces
│   └── src/main/java/com/suplab/aether/core/
│       ├── domain/                # ApiCall, Tenant, value objects (Java records)
│       ├── events/                # Sealed DomainEvent hierarchy
│       ├── ports/                 # MemoryStore, EventPublisher, PolicyRepository
│       └── exceptions/            # AetherException hierarchy
│
├── aether-proxy/                  # Spring Cloud Gateway — Data Plane (port 8080)
│   └── src/main/java/com/suplab/aether/proxy/
│       ├── filter/                # TenantAuthFilter (order=-100), RedactionFilter (order=-90),
│       │                          # ApiCallCaptureFilter (order=-50)
│       ├── outbox/                # JdbcOutboxRepository, OutboxRelayScheduler (5s interval)
│       ├── ratelimit/             # TenantKeyResolver (Redis per-tenant key)
│       └── tenant/                # JdbcTenantRepository
│
├── aether-memory/                 # Embedding service + vector storage
│   └── src/main/java/com/suplab/aether/memory/
│       ├── embedding/             # OllamaEmbeddingService (all-MiniLM-L6-v2, 384-dim)
│       ├── store/                 # PGVectorMemoryStore (<=> cosine operator, strength reinforcement)
│       ├── lifecycle/             # MemoryLifecycleService (daily decay, weekly purge)
│       └── consumer/              # ApiCallMemoryConsumer (Kafka listener, memory type classifier)
│
├── aether-agents/                 # Agent subsystem
│   └── src/main/java/com/suplab/aether/agents/
│       ├── spi/                   # Agent interface, AgentCapability, AgentInput/Output, AgentDecision
│       ├── registry/              # AgentRegistry (Spring List<Agent> injection, disableAgent kill-switch)
│       ├── orchestrator/          # AgentOrchestrator (VirtualThreads, MAX_ITERATIONS=5, Micrometer metrics)
│       ├── governance/            # GovernanceAgent (LLM JSON protocol, confidence gate)
│       ├── retry/                 # RetryAgent (failure memory counts, exponential backoff)
│       ├── hallucination/         # HallucinationDetectorAgent (memory pattern validation)
│       ├── temporal/              # TemporalPredictionAgent (EPISODIC/SEMANTIC counts, ALERT/DEFER)
│       ├── reflection/            # ReflectionAgent (procedural health score, SUGGEST/DEFER)
│       └── llm/                   # LlmClient interface + OllamaLlmClient + GroqLlmClient + AnthropicLlmClient
│
├── aether-policy/                 # Policy engine
│   └── src/main/java/com/suplab/aether/policy/
│       ├── model/                 # PolicyRule, PolicyEvaluationContext, PolicyEvaluationResult
│       ├── engine/                # SpelPolicyEngine (SimpleEvaluationContext, read-only sandbox)
│       ├── storage/               # JdbcPolicyRepository (single-active invariant, auto-versioning)
│       └── audit/                 # AuditLogService (JSONB, no FK), GdprRedactionService (regex PII)
│
├── aether-api/                    # Admin REST API — Control Plane (port 8081)
│   └── src/main/java/com/suplab/aether/api/
│       ├── controller/            # TenantController, PolicyController, MemoryController
│       │                          # GlobalExceptionHandler (RFC 7807 ProblemDetail)
│       ├── config/                # ApiConfig (JdbcApiTenantRepository adapter)
│       └── security/              # SecurityConfig (stateless JWT OAuth2, actuator/Swagger open)
│
├── aether-infra/                  # Infrastructure-as-Code (no Java source)
│   ├── docker/
│   │   ├── docker-compose.yml     # Full local stack (Postgres+pgvector, Redis, Kafka,
│   │   │                          # Zookeeper, Ollama, Prometheus, Grafana)
│   │   ├── docker-compose.test.yml # CI-only (lighter, no Ollama)
│   │   └── .env.example           # All env variable names (no values committed)
│   ├── db/migration/              # Flyway SQL migrations V001–V009+
│   │   ├── V001__create_tenants.sql
│   │   ├── V002__create_endpoints.sql
│   │   ├── V003__create_api_calls.sql
│   │   ├── V004__create_memory_embeddings.sql   # vector(384) column
│   │   ├── V005__create_policies.sql
│   │   ├── V006__create_policy_versions.sql
│   │   ├── V007__create_agent_decisions.sql
│   │   ├── V008__create_audit_log.sql
│   │   └── V009__create_outbox_events.sql
│   ├── k8s/                       # Kubernetes manifests
│   │   ├── deployments/           # aether-proxy, aether-api, aether-agents
│   │   ├── services/
│   │   ├── ingress/
│   │   └── hpa/                   # HorizontalPodAutoscaler
│   └── helm/aether/               # Helm chart for full-stack deployment
│
├── CLAUDE.md                      # Project brief (eeik-bootstrap template)
├── aether.manifest.yaml           # EEIK project manifest
├── pom.xml                        # Parent Maven POM (multi-module)
└── README.md                      # This file
```

---

## Architecture Overview

```
                    ┌──────────────────────────────────────┐
                    │              Aether                   │
                    │                                       │
  Incoming  ──────► │  aether-proxy  (port 8080)           │
  API calls         │  Spring Cloud Gateway                 │
                    │  Tenant routing · Circuit breaker     │
                    │  Rate limiting · Call capture         │
                    └──────────────┬───────────────────────┘
                                   │ ApiCallRecordedEvent (Kafka)
                    ┌──────────────▼───────────────────────┐
                    │           Event Bus (Kafka)           │
                    └───┬───────────────┬──────────────────┘
                        │               │
          ┌─────────────▼──┐   ┌────────▼──────────────────┐
          │ aether-memory   │   │       aether-agents        │
          │ EmbeddingService│   │  AgentRegistry             │
          │ PGVector store  │   │  AgentOrchestrator         │
          │ Knowledge graph │   │  GovernanceAgent           │
          │ Compaction job  │   │  RetryAgent                │
          └─────────────────┘   │  HallucinationAgent        │
                                │  PolicyDriftAgent          │
          ┌─────────────────┐   │  TemporalPredictionAgent   │
          │ aether-policy   │◄──┤  ReflectionAgent           │
          │ Policy-as-Code  │   └────────────────────────────┘
          │ SpEL evaluator  │
          │ GDPR redaction  │
          │ Audit log       │
          └────────┬────────┘
                   │
          ┌────────▼────────┐
          │  aether-api     │  (port 8081)
          │  Admin REST API │
          │  Tenant mgmt    │
          │  OpenTelemetry  │
          └─────────────────┘
```

---

## Agent Mesh

| Agent | Capability | Built | Role |
|---|---|---|---|
| **GovernanceAgent** | `GOVERNANCE` | Phase 7 | LLM JSON response protocol; ALLOW/BLOCK/ALERT decisions; confidence < 0.8 = human-in-the-loop |
| **RetryAgent** | `RETRY_OPTIMIZATION` | Phase 7 | Counts failure/timeout memories; suggests exponential backoff; fast-path for zero-failure calls |
| **HallucinationDetectorAgent** | `HALLUCINATION_DETECTION` | Phase 7 | Validates LLM outputs against stored memory patterns; defaults ALERT when LLM unavailable |
| **TemporalPredictionAgent** | `TEMPORAL_PREDICTION` | Phase 10 | Analyses EPISODIC/SEMANTIC memory counts; LLM ALERT/DEFER predictions; fast-path DEFER for zero memories |
| **ReflectionAgent** | `REFLECTION` | Phase 10 | Procedural health score = `proceduralCount / (total + 1)`; fast-path ALLOW when healthy; LLM SUGGEST when poor |

All agents implement the `Agent` SPI and are auto-discovered via `AgentRegistry` (Spring `List<Agent>` injection). Zero configuration to add a new agent. The `AgentOrchestrator` records `aether.agent.executions` and `aether.agent.latency` Micrometer metrics per execution.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.x (`jakarta.*` exclusively) |
| API Gateway | Spring Cloud Gateway |
| Messaging | Apache Kafka + transactional outbox |
| Database | PostgreSQL 16 + pgvector extension |
| Cache / Rate Limiting | Redis 7 |
| Vector Store | pgvector (default), Chroma (adapter) |
| LLM Runtime | Ollama (default, local) · Groq cloud · Anthropic Claude — swappable via `AETHER_LLM_PROVIDER` env var |
| Embedding Model | all-MiniLM-L6-v2 via Ollama (384-dim, fixed — changing requires full re-embedding migration) |
| Resilience | Resilience4j (circuit breaker, retry, bulkhead) |
| Policy Rules | Spring EL (SpEL) evaluated against YAML policies in PostgreSQL |
| Observability | OpenTelemetry + Micrometer + Prometheus + Grafana |
| DB Migrations | Flyway |
| Build | Maven (multi-module) |
| Local Dev | Docker Compose |
| Production | Kubernetes + Helm |
| CI/CD | GitHub Actions (OIDC, no static secrets) |

---

## Use Cases

- **Enterprise API Governance** — enforce, audit, and evolve API usage policies organisation-wide
- **Engineering Intelligence Platform** — code, architecture, documentation, and release agents
- **Agentic AI Infrastructure** — shared memory and orchestration backbone for any multi-agent system
- **Insurance Claims Platform** — claim analysis, fraud detection, settlement, and communication agents
- **Personal Cognitive Companion** — data and reasoning plane backing Aether Core

---

## Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Agent hallucination | Confidence gate: < 0.8 → human-in-the-loop; HallucinationDetectorAgent validates all outputs |
| Data growth | Monthly `MemoryCompactionJob` summarises old memories; pgvector index pruning |
| Latency | Policy checks async; blocking only on high-confidence enforcement |
| Privacy / GDPR | `GdprRedactionService` strips PII before any persistence; opt-out and erasure endpoints |
| Policy drift | `PolicyDriftAgent` monitors behavioral divergence continuously |
| LLM vendor lock-in | `LlmClient` interface — `OllamaLlmClient` is one adapter; swap freely |

---

## Quick Start

```bash
# Start the full local infrastructure stack
# (PostgreSQL+pgvector, Redis, Kafka, Ollama, Prometheus, Grafana)
docker compose -f aether-infra/docker/docker-compose.yml up -d

# Build all modules (requires Java 21, Maven 3.9+)
mvn verify

# Admin API — Swagger UI (control plane, port 8081)
open http://localhost:8081/api/swagger-ui.html

# Onboard a tenant
curl -s -X POST http://localhost:8081/api/v1/tenants \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{"name":"my-tenant","apiKey":"<raw-key>"}'

# Proxy an API call (data plane, port 8080)
curl -H "X-API-Key: <raw-key>" \
     http://localhost:8080/<configured-path>

# Grafana dashboards
open http://localhost:3000
```

**LLM provider selection** (set before starting `aether-proxy` or `aether-api`):

```bash
# Default: local Ollama
export AETHER_LLM_PROVIDER=ollama

# Groq cloud (fast remote inference)
export AETHER_LLM_PROVIDER=groq
export GROQ_API_KEY=<key>

# Anthropic Claude
export AETHER_LLM_PROVIDER=anthropic
export ANTHROPIC_API_KEY=<key>
```

---

## Documentation

| Document | Description |
|---|---|
| [Concept & Vision](docs/index.html) | Visual overview of the full Aether ecosystem |
| [Architecture](docs/architecture.md) | Technical deep-dive: modules, patterns, data model, security |
| [Roadmap](docs/roadmap.md) | Phased delivery plan (Phase 0–12) |
| [Progress](docs/progress.md) | Live development progress tracker |
| [ADRs](docs/adr/) | Architecture Decision Records |

---

## Contributing

All development follows [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(proxy): add tenant-aware rate limiting
fix(memory): correct cosine similarity threshold
docs(roadmap): mark Phase 2 complete
chore(bootstrap): update .claude/memory/decisions.md
```

Branch from `main`, open a PR. CI must be green. No `// TODO` in committed code.

---

<p align="center">
  <sub>
    Scaffolded and governed by
    <a href="https://github.com/suplab/eeik-bootstrap"><strong>eeik-bootstrap</strong></a>
    — the AI-native enterprise engineering operating system.<br/>
    19 specialist agents · 5 slash commands · persistent memory · governance from day one.
  </sub>
</p>
