#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE="mcr.microsoft.com/playwright/java:v1.63.0-noble"
MAVEN_VOLUME="bank-cookies-tests-maven-repo"

if ! docker image inspect "${IMAGE}" >/dev/null 2>&1; then
  echo "Pulling ${IMAGE} (one-time, ~2 GB with browsers — often 5–15 min)..."
  docker pull "${IMAGE}"
else
  echo "Using cached image ${IMAGE}"
fi

browser="chromium"
if [[ $# -ge 1 && "$1" != -* ]]; then
  browser="$1"
  shift
fi

docker run --rm \
  --volume "${ROOT}:/work" \
  --volume "${MAVEN_VOLUME}:/root/.m2" \
  --workdir /work \
  --env PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 \
  "${IMAGE}" \
  bash /work/scripts/docker-test-inner.sh test "-Dbrowser=${browser}" "$@"
