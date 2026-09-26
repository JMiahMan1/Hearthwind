#!/usr/bin/env bash
# Containerized client gametests (MANDATORY path: never run UI tests on the host).
# Stages host-built jars + sources into /tmp/cgthearthwind-stage (mountable;
# Docker Desktop cannot bind-mount /Users paths), then runs the standard
# harness inside the pinned linux image (java 26 + xvfb + Mesa).
# Screenshots are copied back to the repo .tmp/shots/cgt/ afterwards.
#
# Usage: bash tools/run_client_gametests_container.sh [--keep-dir]
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$DIR/../.." && pwd)"
STAGE="${CGT_STAGE_DIR:-$REPO/.tmp/cgthearthwind-stage}"

CGT_STAGE_DIR="$STAGE" bash "$DIR/stage_container_tests.sh"
docker build -f "$DIR/docker/client-gametest.Dockerfile" -t hearthwind-client-gametest "$DIR/docker"

# Seed a named volume via `docker cp`: Docker Desktop file sharing does not
# reliably serve host bind mounts (shows empty dirs), but the Docker API
# transport always works. Reseed every run so fresh jars are picked up;
# provision/server-jar caches inside the volume survive (cp only overwrites).
docker volume create cgtvol >/dev/null
docker rm -f cgtseed >/dev/null 2>&1 || true
docker create -v cgtvol:/s --name cgtseed alpine true >/dev/null
docker cp "$STAGE/repo" cgtseed:/s/
docker cp "$STAGE/gradle-home" cgtseed:/s/
docker rm cgtseed >/dev/null

set +e
docker run --rm \
  -v cgtvol:/work \
  -w /work/repo/custom-mods \
  -e CGT_ENV=ci \
  -e CGT_XVFB=1 \
  -e CGT_XMX="${CGT_XMX:-3G}" \
  -e CGT_XMS="${CGT_XMS:-1G}" \
  -e CGT_MODID_FILTER="${CGT_MODID_FILTER:-}" \
  -e CGT_EXCLUDE_MODS="${CGT_EXCLUDE_MODS:-}" \
  -e GRADLE_USER_HOME=/work/gradle-home \
  -e "CGT_ARGS=$*" \
  hearthwind-client-gametest \
  bash -lc 'bash /work/repo/custom-mods/tools/run_client_gametests.sh $CGT_ARGS'
RC=$?
set -e

# Pull shots back out through the Docker API (bind mounts can't be trusted).
mkdir -p "$REPO/.tmp/shots/cgt"
docker rm -f cgtshots >/dev/null 2>&1 || true
docker create -v cgtvol:/s --name cgtshots alpine true >/dev/null
docker cp cgtshots:/s/repo/.tmp/shots/cgt/. "$REPO/.tmp/shots/cgt/" 2>/dev/null || true
docker rm cgtshots >/dev/null
mkdir -p "$REPO/.tmp/logs"
docker rm -f cgtlogs >/dev/null 2>&1 || true
docker create -v cgtvol:/s --name cgtlogs alpine true >/dev/null
docker cp cgtlogs:/s/repo/custom-mods/.tmp/logs/cgt-client.log "$REPO/.tmp/logs/cgt-client-container.log" 2>/dev/null || true
docker rm cgtlogs >/dev/null
echo "screenshots copied back: $(ls "$REPO"/.tmp/shots/cgt/*.png 2>/dev/null | wc -l | tr -d ' ')"
exit "$RC"
