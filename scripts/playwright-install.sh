#!/usr/bin/env bash
# OS libraries first, then browser binaries (install --with-deps can validate too early on Linux CI).
set -euo pipefail

browser="${1:?usage: playwright-install.sh <chromium|firefox|webkit>}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

chmod +x mvnw
./mvnw -B -DskipTests compile test-compile --no-transfer-progress

cli() {
  ./mvnw -B exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="$*"
}

if [[ "$(uname -s)" == "Linux" && "${CI:-}" == "true" ]]; then
  sudo apt-get update -qq
fi

echo "Playwright install-deps (${browser})..."
cli "install-deps ${browser}"

echo "Playwright install (${browser})..."
cli "install ${browser}"
