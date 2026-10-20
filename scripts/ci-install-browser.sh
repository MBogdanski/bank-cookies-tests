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

# First Playwright CLI / driver unpack must not pull all browsers (incl. WebKit).
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

playwright_cli() {
  ./mvnw -B exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI \
    -Dexec.args="$*"
}

remove_webkit_cache() {
  rm -rf "${HOME}/.cache/ms-playwright/webkit-"* 2>/dev/null || true
}

echo "=== compile test sources ==="
./mvnw -B -DskipTests compile test-compile --no-transfer-progress

echo "=== CI Playwright setup: ${browser} only ==="
remove_webkit_cache

echo "=== apt (install-deps ${browser}) ==="
sudo apt-get update -qq
playwright_cli "install-deps ${browser}"

remove_webkit_cache

echo "=== browser binary (install ${browser}) ==="
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=0
if [[ "${browser}" == "chromium" ]]; then
  playwright_cli "install chromium --only-shell"
else
  playwright_cli "install ${browser}"
fi
export PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1

remove_webkit_cache

echo "=== cache contents ==="
ls -la "${HOME}/.cache/ms-playwright/" 2>/dev/null || true
if compgen -G "${HOME}/.cache/ms-playwright/webkit-*" > /dev/null 2>&1; then
  echo "ERROR: WebKit cache appeared; refusing to continue." >&2
  exit 1
fi

echo "=== done: ${browser} ==="
