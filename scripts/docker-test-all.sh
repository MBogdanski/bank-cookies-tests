#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
IMAGE="mcr.microsoft.com/playwright/java:v1.63.0-noble"

if ! docker image inspect "${IMAGE}" >/dev/null 2>&1; then
  echo "Pulling ${IMAGE} (one-time, ~2 GB — often 5–15 min)..."
  docker pull "${IMAGE}"
else
  echo "Using cached image ${IMAGE}"
fi

export SKIP_DOCKER_PULL=1
for browser in chromium firefox webkit; do
  echo "========== ${browser} =========="
  ./scripts/docker-test.sh "${browser}"
done
