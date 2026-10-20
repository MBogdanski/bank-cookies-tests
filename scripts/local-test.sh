#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

browser="chromium"
if [[ $# -ge 1 && "$1" != -* ]]; then
  browser="$1"
  shift
fi

if [[ "${SKIP_PLAYWRIGHT_INSTALL:-0}" != 1 ]]; then
  CI="${CI:-}" bash ./scripts/playwright-install.sh "${browser}"
else
  ./mvnw -B install -DskipTests --no-transfer-progress
fi

exec ./mvnw -B test "-Dbrowser=${browser}" "$@"
