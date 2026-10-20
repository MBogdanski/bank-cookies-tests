#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
IMAGE="mcr.microsoft.com/playwright/java:v1.63.0-noble"
MAVEN_VOLUME="bank-cookies-tests-maven-repo"
DOCKER_RUN=(docker run --rm --init --ipc=host)

if ! docker image inspect "${IMAGE}" >/dev/null 2>&1; then
  echo "[$(date -u +%H:%M:%S)] Pulling ${IMAGE} (~2 GB, often 5–15 min on first run)..."
  docker pull --progress=plain "${IMAGE}"
else
  echo "[$(date -u +%H:%M:%S)] Using cached image ${IMAGE}"
fi

run_in_container() {
  "${DOCKER_RUN[@]}" \
    --volume "${ROOT}:/work" \
    --volume "${MAVEN_VOLUME}:/root/.m2" \
    --workdir /work \
    --env PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 \
    "${IMAGE}" \
    "$@"
}

echo "[$(date -u +%H:%M:%S)] Maven install (once for all browsers)..."
run_in_container bash -lc 'chmod +x mvnw && ./mvnw -B install -DskipTests --no-transfer-progress'

for browser in chromium firefox webkit; do
  echo "[$(date -u +%H:%M:%S)] ========== ${browser} =========="
  run_in_container bash -lc "./mvnw -B test -Dbrowser=${browser}"
done

echo "[$(date -u +%H:%M:%S)] All browsers finished."
