#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE="mcr.microsoft.com/playwright/java:v1.63.0-noble"
MAVEN_VOLUME="bank-cookies-tests-maven-repo"

# Required when running Playwright inside Docker (incl. GitHub Actions docker run).
DOCKER_RUN=(docker run --rm --init --ipc=host)

ensure_image() {
  if [[ "${SKIP_DOCKER_PULL:-0}" == 1 ]]; then
    return
  fi
  if ! docker image inspect "${IMAGE}" >/dev/null 2>&1; then
    echo "[$(date -u +%H:%M:%S)] Pulling ${IMAGE} (~2 GB, often 5–15 min on first run)..."
    docker pull --progress=plain "${IMAGE}"
  else
    echo "[$(date -u +%H:%M:%S)] Using cached image ${IMAGE}"
  fi
}

run_in_container() {
  "${DOCKER_RUN[@]}" \
    --volume "${ROOT}:/work" \
    --volume "${MAVEN_VOLUME}:/root/.m2" \
    --workdir /work \
    --env PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 \
    "${IMAGE}" \
    "$@"
}

browser="chromium"
if [[ $# -ge 1 && "$1" != -* ]]; then
  browser="$1"
  shift
fi

ensure_image

run_in_container bash /work/scripts/docker-test-inner.sh test "-Dbrowser=${browser}" "$@"
