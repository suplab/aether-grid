# Aether

> A human-centric cognitive computing framework — philosophy, personal intelligence, and distributed autonomous agents.

**Author:** Suplab &nbsp;|&nbsp; **Status:** Active Development &nbsp;|&nbsp; **Version:** Conceptual Blueprint v1.0

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

Aether Grid is the distributed intelligence layer currently under active development in this repository. It sits as a **smart proxy and governance layer** in front of any API ecosystem:

- **Remembers** every API interaction semantically (embeddings + metadata in PGVector)
- **Learns** patterns of successful and failing requests over time
- **Governs** API usage by generating and enforcing policies-as-code
- **Predicts** temporal failure and latency windows with pre-emptive scheduling
- **Debugs itself**: detects hallucination, policy drift, ineffective retries, and loops
- **Coordinates** specialized agents across a cognitive mesh with shared memory

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
│   ├── agents/                    # 44 specialist agents (eeik-bootstrap)
│   ├── commands/                  # 19 slash commands
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
│       ├── filter/                # ProxyFilter, TenantResolutionFilter, RateLimitFilter
│       ├── circuitbreaker/        # Resilience4j configuration
│       ├── routing/               # DynamicRouteLocator (tenant-aware routes from DB)
│       └── capture/               # CallCaptureService, transactional outbox writer
│
├── aether-memory/                 # Embedding service + vector storage
│   └── src/main/java/com/suplab/aether/memory/
│       ├── embedding/             # EmbeddingService interface + OllamaEmbeddingService
│       ├── store/                 # PgVectorMemoryStore, ChromaMemoryStore (adapter)
│       └── compaction/            # MemoryCompactionJob (monthly scheduled summarizer)
│
├── aether-agents/                 # Agent subsystem
│   └── src/main/java/com/suplab/aether/agents/
│       ├── spi/                   # Agent interface, AgentCapability, AgentInput/Output
│       ├── registry/              # AgentRegistry (Spring List<Agent> injection)
│       ├── orchestrator/          # AgentOrchestrator, OrchestrationPlan
│       ├── governance/            # GovernanceAgent
│       ├── retry/                 # RetryAgent
│       ├── hallucination/         # HallucinationDetectorAgent
│       ├── drift/                 # PolicyDriftAgent
│       ├── temporal/              # TemporalPredictionAgent
│       ├── reflection/            # ReflectionAgent
│       └── llm/                   # LlmClient interface + OllamaLlmClient adapter
│
├── aether-policy/                 # Policy engine
│   └── src/main/java/com/suplab/aether/policy/
│       ├── model/                 # Policy aggregate, PolicyRule, PolicyVersion
│       ├── engine/                # PolicyEngine, SpelRuleEvaluator
│       ├── storage/               # JdbcPolicyStore (versioned YAML in PostgreSQL)
│       └── audit/                 # AuditLog, GdprRedactionService
│
├── aether-api/                    # Admin REST API — Control Plane (port 8081)
│   └── src/main/java/com/suplab/aether/api/
│       ├── tenant/                # TenantController, TenantService
│       ├── policy/                # PolicyController (CRUD + versioning)
│       ├── memory/                # MemoryController (semantic search)
│       ├── metrics/               # MetricsController (aggregated call stats)
│       ├── observability/         # OpenTelemetry + Micrometer config
│       └── security/              # Spring Security 6, JWT + API-key dual auth
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

| Agent | Capability | Role |
|---|---|---|
| **GovernanceAgent** | `GOVERNANCE` | Enforces API policies using LLM + memory. Confidence < 0.8 = human-in-the-loop. |
| **RetryAgent** | `RETRY_OPTIMIZATION` | Learns optimal retry strategies from historical failure patterns. |
| **HallucinationDetectorAgent** | `HALLUCINATION_DETECTION` | Detects when agent outputs contradict stored facts via cosine similarity. |
| **PolicyDriftAgent** | `POLICY_DRIFT` | Monitors live behavior against policy baseline over a rolling 24h window. |
| **TemporalPredictionAgent** | `TEMPORAL_PREDICTION` | Predicts failure/latency windows from time-series memory; feeds RetryAgent. |
| **ReflectionAgent** | `REFLECTION` | Periodic system health evaluation; identifies loops, stale policies, drift. |

All agents implement the `Agent` SPI and are auto-discovered via `AgentRegistry` (Spring `List<Agent>` injection). Zero configuration to add a new agent.

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
| LLM Runtime | Ollama — Gemma2:2b / Phi-3-mini (local-first) |
| Embedding Model | all-MiniLM-L6-v2 via Ollama (384-dim) |
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
docker compose -f aether-infra/docker/docker-compose.yml up -d

# Build all modules
mvn verify

# Admin API (control plane)
open http://localhost:8081/api/swagger-ui.html

# Proxy an API call
curl -H "X-Tenant-ID: my-tenant" \
     -H "X-API-Key: <key>" \
     http://localhost:8080/proxy/my-api/endpoint
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
    44 specialist agents · 19 slash commands · persistent memory · governance from day one.
  </sub>
</p>
