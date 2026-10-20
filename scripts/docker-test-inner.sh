#!/usr/bin/env bash
set -euo pipefail

cd /work
chmod +x mvnw
./mvnw -B install -DskipTests --no-transfer-progress
exec ./mvnw -B "$@"
