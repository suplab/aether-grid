#!/usr/bin/env bash
# On-stop hook — appends a brief session summary to session-log.md
# Called by Claude Code when the session ends

SESSION_LOG=".claude/memory/session-log.md"
TIMESTAMP=$(date -u '+%Y-%m-%d %H:%M UTC')

if [ -f "$SESSION_LOG" ]; then
  # Only append if the today's date section doesn't already have an "on-stop" marker
  TODAY=$(date -u '+%Y-%m-%d')
  if ! grep -q "on-stop:$TODAY" "$SESSION_LOG" 2>/dev/null; then
    cat >> "$SESSION_LOG" << EOF

---
<!-- on-stop:$TODAY -->
_Session ended at $TIMESTAMP. Review open items above and update progress.md if phases were completed._
EOF
  fi
fi

exit 0
