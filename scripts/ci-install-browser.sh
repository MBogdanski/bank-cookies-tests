#!/usr/bin/env bash
set -euo pipefail

browser="${1:?usage: ci-install-browser.sh chromium|firefox}"
if [[ "${browser}" != "chromium" && "${browser}" != "firefox" ]]; then
  echo "Unsupported browser for CI: ${browser}" >&2
  exit 1
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
chmod +x mvnw

playwright_cli() {
  # Pass subcommand + args as one exec.args string (Playwright CLI parses it).
  ./mvnw -B exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI \
    -Dexec.args="$*"
}

remove_webkit_cache() {
  rm -rf "${HOME}/.cache/ms-playwright/webkit-"* 2>/dev/null || true
}

echo "=== CI Playwright setup: ${browser} only ==="
remove_webkit_cache

echo "=== apt (install-deps ${browser}) ==="
sudo apt-get update -qq
playwright_cli "install-deps ${browser}"

remove_webkit_cache

echo "=== browser binary (install ${browser}) ==="
if [[ "${browser}" == "chromium" ]]; then
  playwright_cli "install chromium --only-shell"
else
  playwright_cli "install ${browser}"
fi

remove_webkit_cache

echo "=== cache contents ==="
ls -la "${HOME}/.cache/ms-playwright/" 2>/dev/null || true
if compgen -G "${HOME}/.cache/ms-playwright/webkit-*" > /dev/null 2>&1; then
  echo "ERROR: WebKit cache appeared; refusing to continue." >&2
  exit 1
fi

echo "=== done: ${browser} ==="
