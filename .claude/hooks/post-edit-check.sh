#!/usr/bin/env bash
# Post-edit quality check — warns on common violations of golden rules
# Called by Claude Code after editing Java files

FILE_PATH="$1"

# Only check Java files
if ! echo "$FILE_PATH" | grep -q '\.java$'; then
  exit 0
fi

WARNINGS=()

# Warn on field @Autowired
if grep -q '@Autowired' "$FILE_PATH" 2>/dev/null; then
  WARNINGS+=("WARNING: Field @Autowired found in $FILE_PATH — use constructor injection instead.")
fi

# Warn on javax.* imports (Spring Boot 3.x uses jakarta.*)
if grep -q 'import javax\.' "$FILE_PATH" 2>/dev/null; then
  WARNINGS+=("WARNING: javax.* import found in $FILE_PATH — use jakarta.* for Spring Boot 3.x.")
fi

# Warn on SELECT *
if grep -qi 'SELECT \*' "$FILE_PATH" 2>/dev/null; then
  WARNINGS+=("WARNING: SELECT * found in $FILE_PATH — use explicit column lists.")
fi

# Warn on System.out
if grep -q 'System\.out\.' "$FILE_PATH" 2>/dev/null; then
  WARNINGS+=("WARNING: System.out found in $FILE_PATH — use SLF4J logger instead.")
fi

# Warn on TODO comments
if grep -q '// TODO' "$FILE_PATH" 2>/dev/null; then
  WARNINGS+=("WARNING: // TODO found in $FILE_PATH — resolve before committing.")
fi

if [ ${#WARNINGS[@]} -gt 0 ]; then
  echo "--- Aether Code Quality Warnings ---" >&2
  for w in "${WARNINGS[@]}"; do
    echo "$w" >&2
  done
  echo "--- Review CLAUDE.md golden rules ---" >&2
fi

exit 0
