# Project Context — Aether

## Repository
- **Name:** aether
- **GitHub:** suplab/aether
- **Branch (active dev):** claude/enterprise-app-planning-setup-whtxmu
- **Author:** Suplab

## What This Is
Aether is a human-centric cognitive computing framework. This repository implements **Aether Grid** — the distributed intelligence layer — as a Maven multi-module Spring Boot 3.x / Java 21 application.

## Service Inventory

| Service | Module | Port | Role |
|---|---|---|---|
| aether-proxy | `aether-proxy` | 8080 | Data Plane — Spring Cloud Gateway, intercepts all API traffic |
| aether-api | `aether-api` | 8081 | Control Plane — Admin REST API for tenants, policies, memory, metrics |
| aether-memory | `aether-memory` | (library) | Embedding service + PGVector storage |
| aether-agents | `aether-agents` | (library) | Agent SPI, registry, orchestrator, all agent implementations |
| aether-policy | `aether-policy` | (library) | Policy engine, SpEL evaluation, GDPR redaction, audit log |
| aether-core | `aether-core` | (library) | Shared domain models, sealed events, port interfaces |
| aether-infra | `aether-infra` | (no Java) | Docker Compose, Flyway migrations, K8s manifests, Helm |

## Infrastructure (local Docker Compose)

| Service | Port | Purpose |
|---|---|---|
| PostgreSQL 16 + pgvector | 5432 | Primary DB + vector store |
| Redis 7 | 6379 | Cache, rate limiting, TemporalPrediction cache |
| Apache Kafka | 9092 | Event streaming (transactional outbox relay) |
| Zookeeper | 2181 | Kafka coordination |
| Ollama | 11434 | Local LLM runtime (Gemma2:2b / Phi-3-mini + all-MiniLM) |
| Prometheus | 9090 | Metrics scraping |
| Grafana | 3000 | Dashboards |

## Kafka Topics

| Topic | Producer | Consumers |
|---|---|---|
| `aether.api.calls` | aether-proxy (outbox relay) | aether-memory, aether-agents |
| `aether.agent.decisions` | aether-agents | aether-policy, aether-api |
| `aether.policy.updated` | aether-policy | aether-proxy (route refresh) |
| `aether.governance.updated` | aether-agents (GovernanceAgent) | aether-proxy |

## Authentication

| Method | Where used |
|---|---|
| `X-API-Key` header (SHA-256 hash vs DB) | Service-to-service; tenant onboarding |
| JWT (Spring Security 6 Resource Server) | Human admin users via aether-api |
| PG session variable `app.current_tenant_id` | Row-level security enforcement |

## Environment Variables (never committed, see `.env.example`)
- `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `REDIS_URL`
- `KAFKA_BOOTSTRAP_SERVERS`
- `OLLAMA_BASE_URL` (default: `http://localhost:11434`)
- `OLLAMA_MODEL` (default: `gemma2:2b`)
- `OLLAMA_EMBEDDING_MODEL` (default: `all-minilm`)
- `JWT_SECRET` / `JWT_ISSUER`

## Local URLs (when Docker Compose is running)
- Proxy: `http://localhost:8080`
- Admin API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/api/swagger-ui.html`
- Grafana: `http://localhost:3000`
- Prometheus: `http://localhost:9090`
- Ollama: `http://localhost:11434`

## Database
- **DB:** PostgreSQL 16
- **Schema:** `aethergrid`
- **Migrations:** Flyway, files in `aether-infra/db/migration/`
- **JDBC:** `NamedParameterJdbcTemplate` + Spring Data JDBC (no JPA/Hibernate)
- **Vector column:** `vector(384)` in `memory_embeddings.embedding`
