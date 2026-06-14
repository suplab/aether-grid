# /review

Review the current diff or specified files against Aether's golden rules and patterns.

## Checklist

### Golden Rules
- [ ] Constructor injection only — no field `@Autowired`
- [ ] No hardcoded secrets or connection strings
- [ ] SLF4J parameterized logging — no `System.out`, no string concatenation
- [ ] SOLID principles respected
- [ ] DDD bounded contexts — no reaching into other modules' internals
- [ ] No `SELECT *` — explicit column lists in all SQL
- [ ] Parameterized queries — `NamedParameterJdbcTemplate` with named params
- [ ] `jakarta.*` only — no `javax.*`
- [ ] No `// TODO` in committed code

### Aether-Specific
- [ ] Agent confidence gate respected (< 0.8 → never auto-block)
- [ ] PII routed through `GdprRedactionService` before persistence
- [ ] `tenant_id` in all DB queries that touch tenant data
- [ ] LLM calls go through `LlmClient` interface — not Ollama directly
- [ ] New Kafka events use the transactional outbox pattern

### Quality
- [ ] Tests cover happy path, error cases, and edge cases
- [ ] No empty catch blocks
- [ ] No `Optional.get()` without guard
- [ ] No `Thread.sleep()` in tests

### Docs sync
- [ ] `docs/progress.md` updated if a phase deliverable is complete
- [ ] `docs/architecture.md` updated if data model or patterns changed
- [ ] `README.md` updated if scope or tech stack changed

## Output Format

List findings as:
- 🔴 BLOCKER — violates a golden rule
- 🟡 WARNING — pattern deviation worth fixing
- 🟢 SUGGESTION — nice to have
