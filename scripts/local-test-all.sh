#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

for browser in chromium firefox webkit; do
  echo "========== ${browser} =========="
  ./scripts/local-test.sh "${browser}"
done
