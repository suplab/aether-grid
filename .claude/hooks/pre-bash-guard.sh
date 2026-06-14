#!/usr/bin/env bash
# Pre-bash safety guard — blocks destructive operations
# Called by Claude Code before executing bash commands

COMMAND="$1"

# Block force-push to main
if echo "$COMMAND" | grep -qE 'git push.*--force.*main|git push.*-f.*main'; then
  echo "BLOCKED: Force-push to main is not allowed." >&2
  exit 1
fi

# Block hard reset
if echo "$COMMAND" | grep -qE 'git reset --hard'; then
  echo "BLOCKED: git reset --hard requires explicit user confirmation." >&2
  exit 1
fi

# Block DROP TABLE / TRUNCATE (without explicit confirmation keyword)
if echo "$COMMAND" | grep -qiE 'DROP TABLE|TRUNCATE TABLE'; then
  if ! echo "$COMMAND" | grep -q 'CONFIRMED_DESTRUCTIVE'; then
    echo "BLOCKED: DROP TABLE / TRUNCATE requires CONFIRMED_DESTRUCTIVE keyword." >&2
    exit 1
  fi
fi

# Block deletion of src directories
if echo "$COMMAND" | grep -qE 'rm -rf.*(src|main|aether-)'; then
  echo "BLOCKED: Recursive deletion of source directories is not allowed." >&2
  exit 1
fi

exit 0
