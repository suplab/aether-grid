# /estimate

Produce a P50/P80/P90 effort estimate for the described task.

## Formula
Human Days = Raw Hours ÷ 6.4
- P50 × 1.0 (most likely)
- P80 × 1.3 (80% confidence)
- P90 × 1.6 (90% confidence)

6.4 = 8 hrs/day × 80% efficiency factor (meetings, reviews, interruptions)

## Output Format

```
Task: <task description>

Scope analysis:
- <what needs to be built / changed>
- <integration points>
- <test requirements>

Effort breakdown:
| Component | Raw Hours |
|-----------|-----------|
| <component> | N |
| Tests | N |
| Docs update | N |
| **Total raw** | **N** |

Estimates:
| Confidence | Human Days |
|------------|------------|
| P50 | X.X |
| P80 | X.X |
| P90 | X.X |

Assumptions:
- <assumption 1>
- <assumption 2>

Risks that could push to P90:
- <risk 1>
```
