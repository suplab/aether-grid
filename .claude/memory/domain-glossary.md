# Domain Glossary — Aether

| Term | Definition | Do NOT confuse with |
|---|---|---|
| **Aether** | The full ecosystem: philosophy + Aether Core + Aether Grid. The invisible cognitive fabric. | The repo name (same word, different scope) |
| **Aether Core** | The personal cognitive engine — individual mind OS. Not yet implemented in this repo. | `aether-core` Maven module (which is the shared domain layer for Aether Grid) |
| **Aether Grid** | The distributed intelligence layer — cognitive mesh of agents. What this repo builds. | The repo name (repo is now called "aether") |
| **Cognitive Mesh** | Aether Grid's architecture style: agents share memory and reasoning rather than isolated microservices | Service mesh (network-layer, no shared reasoning) |
| **ApiCall** | Central aggregate root: a single captured HTTP request+response pair with full metadata | A Java method call |
| **MemoryRecord** | A semantic embedding + metadata stored in pgvector. Implements the 4 memory types. | A Java `record` (though MemoryRecord IS implemented as a Java record) |
| **EmbeddingVector** | A float[384] produced by all-MiniLM-L6-v2 representing the semantic meaning of text | Any random float array |
| **Agent** | A Spring `@Component` implementing the `Agent` SPI. Autonomous reasoning unit with a specific capability. | A human agent; a Spring Security principal; a Claude Code agent |
| **AgentCapability** | An enum value declaring what kind of reasoning an agent performs (GOVERNANCE, RETRY_OPTIMIZATION, etc.) | A Java capability or permission |
| **AgentOrchestrator** | The coordinator that builds an OrchestrationPlan and dispatches AgentInputs to the right agents | A Kubernetes orchestrator |
| **GovernanceAgent** | The agent that enforces API usage policies using LLM reasoning + memory retrieval | A human governance officer |
| **RetryAgent** | The agent that learns optimal retry strategies from historical failure patterns | A simple Spring Retry annotation |
| **HallucinationDetectorAgent** | Detects when an agent's LLM output contradicts verifiable stored facts | The medical condition |
| **PolicyDriftAgent** | Detects when live API behavior diverges statistically from the baseline encoded in the active policy | Infrastructure/config drift |
| **TemporalPredictionAgent** | Predicts future time windows where an API is likely to fail or degrade | A network timeout window |
| **ReflectionAgent** | Periodically evaluates system-level health; identifies loops, stale policies, underperforming agents | Java reflection (`Class.forName`) |
| **Policy** | A versioned set of governance rules for a tenant, stored as YAML in PostgreSQL | Spring Security policy; AWS IAM policy |
| **PolicyRule** | A single rule within a Policy: a SpEL condition + action + severity | A business rule engine rule |
| **PolicyDrift** | When live API behavior diverges significantly from the baseline encoded in the active Policy | Config drift in infrastructure |
| **Tenant** | An organization that has onboarded one or more API endpoints under Aether governance | A DB schema tenant (though tenant isolation IS implemented at the DB level) |
| **Control Plane** | `aether-api` — the admin REST API for configuring tenants, policies, and viewing metrics | The Kubernetes control plane |
| **Data Plane** | `aether-proxy` — the Spring Cloud Gateway that intercepts live API traffic | A network data plane |
| **Transactional Outbox** | The pattern where ApiCall rows and Kafka event payloads are written in one DB transaction; a relay publishes to Kafka | Direct KafkaTemplate.send() (which is the anti-pattern to avoid) |
| **CompactionJob** | The monthly scheduled job that summarizes old MemoryRecords via LLM to control storage growth | Data compression or ZIP |
| **Human-in-the-Loop** | The hard constraint that agents with confidence < 0.8 NEVER automatically block an API call | An optional UX review step |
| **ConfidenceThreshold** | The minimum confidence (0.8) an agent must have before its blocking decision is executed | A statistical significance threshold |
| **SpEL** | Spring Expression Language — the rule evaluation engine for Policy conditions | A separate DSL or scripting language |
| **pgvector** | PostgreSQL extension that adds `vector(N)` column type and cosine/L2 distance operators | A separate vector database like Chroma or Pinecone |
| **PGVectorMemoryStore** | The adapter implementing `MemoryStore` using pgvector's `<->` cosine distance operator | ChromaMemoryStore (the alternative adapter) |
| **OllamaLlmClient** | The adapter implementing `LlmClient` that calls Ollama's `/api/generate` endpoint | A hardcoded LLM dependency |
| **GdprRedactionService** | Scans call data for PII (email, phone, card) and replaces with `[REDACTED]` before persistence | Optional data masking |
| **AuditLog** | The immutable append-only table recording every policy change, governance decision, and GDPR action | Application logs (which are ephemeral) |
| **DataLineage** | Tracking every transformation applied to a call (redaction, embedding, policy eval) for "explain this decision" auditing | Data lineage in ETL pipelines |
| **FailureWindow** | A predicted time range during which a governed API is likely to fail or degrade | A network timeout window |
| **OrchestrationPlan** | A value object describing which AgentCapabilities to invoke and in what order for a given ApiCall | A Kubernetes deployment plan |
