# /security-scan

Run a security review of the current changes or specified module.

## Checklist

### Secrets & Credentials
- [ ] No hardcoded passwords, tokens, API keys, or connection strings
- [ ] `.env` is in `.gitignore`; only `.env.example` is committed
- [ ] `application.yml` uses `${ENV_VAR}` references, not literal values

### SQL Injection
- [ ] All queries use `NamedParameterJdbcTemplate` with `:namedParams`
- [ ] No string concatenation in SQL anywhere
- [ ] No raw `Statement` usage

### Authentication & Authorization
- [ ] All write endpoints have `@PreAuthorize` annotations
- [ ] Tenant isolation enforced: `tenant_id` in all data-access queries
- [ ] JWT validation configured in `ApiSecurityConfig`
- [ ] API key hashed (SHA-256) before storage — never stored in plaintext

### PII / GDPR
- [ ] `GdprRedactionService.redact()` called before any persistence of call data
- [ ] No PII in log messages
- [ ] GDPR erasure endpoint implemented for affected entities

### LLM Safety
- [ ] Agent inputs sanitized before prompt construction
- [ ] LLM outputs validated by `HallucinationDetectorAgent` where applicable
- [ ] Confidence gate enforced at `AgentOrchestrator` — no auto-block below 0.8

### Dependencies
- [ ] No known CVEs in new dependencies (OWASP Dependency Check)
- [ ] No transitive `javax.*` pulls in from new deps

## Output Format
- 🔴 CRITICAL — must fix before merge
- 🟡 HIGH — fix in this sprint
- 🟢 MEDIUM — track in tech-debt.md
