#!/usr/bin/env bash
# Containerized server gametests (MANDATORY path: never run game tests on the host).
# Host builds first (./gradlew build), then this stages jars + data and runs
# the standard harness inside the pinned linux image (java 26).
#
# Usage: bash tools/run_gametests_container.sh [--keep-server]
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$DIR/../.." && pwd)"
STAGE="${CGT_STAGE_DIR:-/tmp/cgthearthwind-stage}"

bash "$DIR/stage_container_tests.sh"
docker build -f "$DIR/docker/server-gametest.Dockerfile" -t hearthwind-server-gametest "$DIR/docker"

docker volume create cgtvol >/dev/null
docker rm -f cgtseed >/dev/null 2>&1 || true
docker create -v cgtvol:/s --name cgtseed alpine true >/dev/null
docker cp "$STAGE/repo" cgtseed:/s/
docker cp "$STAGE/gradle-home" cgtseed:/s/
docker rm cgtseed >/dev/null

docker run --rm \
  -v cgtvol:/work \
  -w /work/repo/custom-mods \
  -e SKIP_BUILD=1 \
  -e JAVA_HOME=/opt/java/openjdk \
  -e GRADLE_USER_HOME=/work/gradle-home \
  -e "CGT_SRV_ARGS=$*" \
  hearthwind-server-gametest \
  bash -lc 'bash /work/repo/custom-mods/tools/run_gametests.sh $CGT_SRV_ARGS'
