# /memory-update

Update `.claude/memory/` files after a significant session, decision, or milestone.

## When to run
- After completing a phase
- After a major architectural decision
- After discovering a new pattern or anti-pattern
- After a significant bug fix that reveals a constraint

## Files to review and update

| File | Update when |
|---|---|
| `project-context.md` | New service, port, topic, or environment added |
| `domain-glossary.md` | New domain term introduced |
| `decisions.md` | Architectural decision made |
| `constraints.md` | New hard constraint discovered |
| `patterns.md` | New pattern introduced or existing pattern evolved |
| `tech-debt.md` | Debt accepted or resolved |
| `session-log.md` | Always — add a brief bullet for what was done |

## Format for session-log.md entry

```
## YYYY-MM-DD

**Phase N — <Phase Name>**
- <what was done>
- <what was decided>
- Committed: `<commit message>`

**Open items for next session:**
- <item 1>
```
