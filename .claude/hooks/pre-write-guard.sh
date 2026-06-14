#!/usr/bin/env bash
# Pre-write safety guard — blocks writes to sensitive paths
# Called by Claude Code before writing files

FILE_PATH="$1"

# Block writes to .env (values, not .env.example)
if echo "$FILE_PATH" | grep -qE '\.env$'; then
  echo "BLOCKED: Writing .env directly is not allowed. Use .env.example for templates." >&2
  exit 1
fi

# Block writes to secrets directories
if echo "$FILE_PATH" | grep -qE '(secrets|credentials|\.aws/credentials)'; then
  echo "BLOCKED: Writing to secrets paths is not allowed." >&2
  exit 1
fi

exit 0
