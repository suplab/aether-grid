# Constraints — Aether

Hard constraints that cannot be compromised. These are enforced by EEIK hooks and code review.

---

## EEIK Golden Rules (All Mandatory)

1. **Constructor injection exclusively** — no `@Autowired` on fields, no `@Inject`. All injected fields must be `final`.
2. **No hardcoded secrets** — credentials, API keys, connection strings go to environment variables or AWS Secrets Manager. Never committed to source.
3. **SLF4J with parameterized messages** — `log.info("Processing call {}", callId)` not `log.info("Processing call " + callId)`. Never `System.out.println()`.
4. **SOLID design principles** — Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion.
5. **DDD bounded contexts** — cross-module calls go through port interfaces (`MemoryStore`, `EventPublisher`, etc.). Never reach into another module's internal classes.
6. **Explicit SQL column lists** — never `SELECT *`. Always name every column in `SELECT`, `INSERT`, and `UPDATE` statements.
7. **Parameterized queries** — no string concatenation in SQL. Use `NamedParameterJdbcTemplate` with named parameters.
8. **Conventional Commits** — all commits follow `type(scope): description`. Types: `feat`, `fix`, `docs`, `chore`, `build`, `test`, `refactor`, `perf`.
9. **No `// TODO` in committed code** — if it's not complete, don't commit it. Use a tracked issue instead.
10. **`jakarta.*` exclusively** — Spring Boot 3.x uses `jakarta.*`. Any `javax.*` import is a compile error in this project.

---

## Aether-Specific Hard Constraints

### Human-in-the-Loop (Agent Confidence Gate)
- Agent decisions with confidence < 0.8 **MUST NOT** automatically block an API call.
- The confidence gate is enforced at `AgentOrchestrator` level — not inside individual agents.
- This is a non-negotiable safety constraint from the architectural blueprint.

### PII Redaction Before Persistence
- All call data (request bodies, headers, response bodies) **MUST** pass through `GdprRedactionService` before being written to `api_calls`, `memory_embeddings`, or `audit_log`.
- PII includes: email addresses, phone numbers, credit card numbers, national ID numbers.
- Redaction is `[REDACTED]`, not deletion — the field presence is preserved.

### Tenant Isolation
- Every SQL query that touches tenant-owned data **MUST** include `WHERE tenant_id = :tenantId`.
- PostgreSQL Row-Level Security policies reinforce this at the DB layer (enabled in Phase 11).
- A service must only access data belonging to the authenticated tenant's `TenantId`.
- Cross-tenant queries are forbidden outside of `ADMIN` role operations.

### LLM Abstraction
- All LLM calls **MUST** go through the `LlmClient` interface.
- `OllamaLlmClient` is one adapter. No agent may call Ollama directly.
- Model name is always read from `OLLAMA_MODEL` environment variable — never hardcoded.

### Vector Dimensionality
- Memory embeddings use 384-dimensional vectors (all-MiniLM-L6-v2 output).
- The `vector(384)` column type in `memory_embeddings` is set at migration time.
- Changing embedding models requires a full re-embedding migration — this is a major version change, not a config change.

### No JPA / Hibernate
- Spring Data JDBC and `NamedParameterJdbcTemplate` only.
- JPA/Hibernate is explicitly excluded from the dependency graph. Its session management and lazy-loading behavior is incompatible with the reactive proxy layer and creates N+1 query risks.

### Immutable Audit Log
- Rows in `audit_log` are append-only. No `UPDATE` or `DELETE` is permitted.
- GDPR erasure marks records as erased but does not delete the audit row itself.

### Secrets Never in Source
- `.env` files are in `.gitignore`.
- Only `.env.example` (with variable names, no values) is committed.
- OIDC is used for GitHub Actions → GHCR push. No static tokens in CI.

---

## Operational Constraints

- **Local-first:** The system must run fully offline with Ollama. No external LLM API calls are required in the default configuration.
- **Monthly compaction:** `MemoryCompactionJob` must run before storage exceeds defined growth limits. It runs on the 1st of each month at 02:00 UTC.
- **Flyway forward-only:** DB migrations are irreversible. Design them to be backwards-compatible with the previous application version during rolling deploys.
- **Readiness before liveness:** Spring Boot actuator healthchecks must verify DB, Kafka, and Redis connectivity before reporting `UP`.
