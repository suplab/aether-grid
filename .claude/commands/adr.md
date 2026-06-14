# /adr

Create an Architecture Decision Record for a significant technical decision.

## Output Location
`docs/adr/NNN-<kebab-case-title>.md`

## Template

```markdown
# ADR-NNN — <Title>

**Date:** YYYY-MM-DD
**Status:** Proposed | Accepted | Superseded | Deprecated
**Deciders:** suplab
**Supersedes:** (ADR-NNN if applicable)

## Context

<What is the issue or decision that needs to be made? What forces are at play?>

## Decision

<What was decided?>

## Consequences

### Positive
- <benefit 1>

### Negative / Trade-offs
- <trade-off 1>

## Alternatives Considered

| Alternative | Why rejected |
|---|---|
| <option> | <reason> |

## References
- `docs/architecture.md`
- Related code: `<module>/<class>`
```

## After creating the ADR
- Add an entry to `.claude/memory/decisions.md`
- Update `docs/architecture.md` if relevant
- Reference the ADR number in the commit message: `docs(adr): add ADR-NNN ...`
