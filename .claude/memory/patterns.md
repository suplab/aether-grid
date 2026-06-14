# Approved Patterns — Aether

Patterns in active use in this codebase. When implementing new features, reuse these before inventing alternatives.

---

## 1. Agent Plugin Pattern (SPI via Spring List injection)

**Where:** `aether-agents` module, `AgentRegistry`

**Pattern:**
```java
// SPI interface — in aether-agents/spi/
public interface Agent {
    boolean canHandle(AgentCapability capability);
    AgentOutput process(AgentInput input);
}

// Registry — discovers all Agent beans automatically
@Component
public class AgentRegistry {
    private final List<Agent> agents;

    public AgentRegistry(List<Agent> agents) { // Spring injects all @Component impls
        this.agents = agents;
    }

    public List<Agent> findByCapability(AgentCapability capability) {
        return agents.stream().filter(a -> a.canHandle(capability)).toList();
    }
}

// New agent — zero config required
@Component
public class MyNewAgent implements Agent { ... }
```

**Rule:** Never add agents to a hardcoded list or switch-case. Drop in a new `@Component` — that's all.

---

## 2. Hexagonal Architecture (Ports & Adapters)

**Where:** Everywhere — port interfaces in `aether-core`, adapters in each feature module

**Pattern:**
```java
// Port (in aether-core — no implementation)
public interface MemoryStore {
    void store(MemoryRecord record);
    List<MemoryRecord> findSimilar(EmbeddingVector query, int topK);
}

// Adapter (in aether-memory — implements the port)
@Component
public class PgVectorMemoryStore implements MemoryStore {
    private final NamedParameterJdbcTemplate jdbc;

    public PgVectorMemoryStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<MemoryRecord> findSimilar(EmbeddingVector query, int topK) {
        // Uses pgvector <-> cosine distance, explicit column list, parameterized query
    }
}
```

**Rule:** Port interfaces live in `aether-core`. Adapters live in the module that provides the implementation. Agent code depends on the port, never the adapter.

---

## 3. Transactional Outbox (Reliable Kafka Publishing)

**Where:** `aether-proxy`, `CallCaptureService`

**Pattern:**
```java
@Transactional
public void captureCall(ApiCall call) {
    // Both writes in ONE transaction — atomically
    apiCallRepository.save(call);                     // api_calls table
    outboxRepository.save(OutboxEvent.from(call));    // outbox_events table
    // Kafka is NOT called here
}

// Separate relay thread/scheduled job
@Scheduled(fixedDelay = 500)
public void relayOutboxEvents() {
    List<OutboxEvent> pending = outboxRepository.findUnpublished();
    for (OutboxEvent event : pending) {
        kafkaTemplate.send(event.topic(), event.payload());
        outboxRepository.markPublished(event.id());
    }
}
```

**Rule:** Never call `KafkaTemplate.send()` inside a `@Transactional` method that also writes to the DB. Use the outbox.

---

## 4. Policy-as-Code (SpEL in YAML stored in PostgreSQL)

**Where:** `aether-policy`, `SpelRuleEvaluator`, `JdbcPolicyStore`

**Pattern:**
```yaml
# Stored as YAML text in policies.yaml_content column
id: latency-alert
rules:
  - name: high-latency
    condition: "#call.metrics.latencyMs > 2000 && #call.outcome == 'FAILURE'"
    action: ALERT
    severity: HIGH
```

```java
// SpEL evaluation — sandboxed context
StandardEvaluationContext context = new StandardEvaluationContext();
context.setVariable("call", apiCall); // Only ApiCall properties exposed
Expression expression = parser.parseExpression(rule.condition());
boolean triggered = expression.getValue(context, Boolean.class);
```

**Rule:** Policy conditions are always text in the DB, never hardcoded in Java. Changing governance rules never requires redeployment.

---

## 5. Memory-Augmented LLM Prompt

**Where:** `aether-agents`, `LlmPromptBuilder`

**Pattern:**
```java
// Every agent prompt includes:
// 1. Instruction (what to do)
// 2. Active policy for the tenant
// 3. Top-K similar past interactions from memory
// 4. Current API call context
String prompt = LlmPromptBuilder.builder()
    .instruction("Evaluate if this API call violates governance policy.")
    .policy(activePolicy.toYaml())
    .memories(memoryService.findSimilarCalls(call.toText(), 10))
    .call(call)
    .build();
```

**Rule:** No agent calls the LLM without first retrieving relevant memories. Context-free LLM calls produce lower-quality governance decisions.

---

## 6. Sealed Domain Event Hierarchy

**Where:** `aether-core`, `events/`

**Pattern:**
```java
// Sealed — compiler enforces exhaustive handling
public sealed interface DomainEvent
    permits ApiCallRecordedEvent, PolicyViolatedEvent,
            AgentDecisionEvent, GovernanceUpdatedEvent { }

// Java 21 pattern matching — exhaustive switch
switch (event) {
    case ApiCallRecordedEvent e -> handleApiCall(e);
    case PolicyViolatedEvent e -> handleViolation(e);
    case AgentDecisionEvent e -> handleDecision(e);
    case GovernanceUpdatedEvent e -> handleGovernance(e);
    // No default needed — compiler verifies all cases covered
}
```

**Rule:** New event types must be added to the `permits` clause. This forces all consumers to handle them.

---

## 7. Confidence Gate in Orchestrator

**Where:** `aether-agents`, `AgentOrchestrator`

**Pattern:**
```java
AgentOutput output = agent.process(input);

if (output.confidence() >= CONFIDENCE_THRESHOLD && output.action() == BLOCK) {
    // Safe to auto-enforce
    enforceBlock(output);
} else {
    // Below threshold — flag for human review, never auto-block
    flagForHumanReview(output);
    metricsRegistry.counter("aether.agents.human_review_required").increment();
}
```

**Rule:** The threshold constant is defined once in `AgentOrchestrator`. Never replicate this check inside individual agents.

---

## 8. GDPR Redaction Pipeline

**Where:** `aether-policy`, `GdprRedactionService` — called before any persistence

**Pattern:**
```java
// Always redact before saving
ApiCall redacted = gdprRedactionService.redact(rawCall);
apiCallRepository.save(redacted);
memoryService.store(redacted); // Embedding of redacted content, not raw
```

**Rule:** Raw call data (with possible PII) is never written to the DB. `GdprRedactionService.redact()` is always called first in the capture pipeline.
