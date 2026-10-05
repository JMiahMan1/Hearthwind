#!/usr/bin/env bash
# Containerized client gametests (MANDATORY path: never run UI tests on the host).
# Stages host-built jars + sources into $REPO/.tmp/cgthearthwind-stage and
# BIND-MOUNTS that tree, so the container reads exactly what the repo stages
# and nothing else. No named volume and no `docker cp`: the image is pinned,
# the inputs are files, and what the container writes lands back in the stage
# where a host-side cp can pick it up.
#
# Usage: bash tools/run_client_gametests_container.sh [--keep-dir]
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$DIR/../.." && pwd)"
STAGE="${CGT_STAGE_DIR:-$REPO/.tmp/cgthearthwind-stage}"

# Docker on this host is Colima, whose QEMU VM holds 8 GB of RAM for as long
# as it runs, and whose image/volume leftovers reached 29 GB. An 8 GB VM left
# up for a day pushed this 32 GB machine into swap exhaustion, and the client
# then died with SIGSEGV in the C2 compiler purely because native allocation
# could not be satisfied. So: remember whether the VM was already up, and
# always clean up on exit. CGT_KEEP=1 skips cleanup for a debugging session.
colima status >/dev/null 2>&1 && CGT_VM_PREEXISTING=1 || CGT_VM_PREEXISTING=0
if [ "$CGT_VM_PREEXISTING" = 0 ]; then
  echo "starting colima for this run (the cleanup trap stops it again)"
  colima start >/dev/null 2>&1 || colima start
fi
cleanup_container_tests() {
  if [ "${CGT_KEEP:-0}" = 1 ]; then
    echo "CGT_KEEP=1 - leaving test images and volumes in place"
    return 0
  fi
  if [ "$CGT_VM_PREEXISTING" = 0 ]; then
    bash "$DIR/cleanup_container_tests.sh" --stop-vm
  else
    bash "$DIR/cleanup_container_tests.sh"
  fi
}
trap cleanup_container_tests EXIT


CGT_STAGE_DIR="$STAGE" bash "$DIR/stage_container_tests.sh"
docker build -f "$DIR/docker/client-gametest.Dockerfile" -t hearthwind-client-gametest "$DIR/docker"

set +e
docker run --rm \
  -v "$STAGE:/work" \
  -w /work/repo/custom-mods \
  -e CGT_ENV=ci \
  -e CGT_XVFB=1 \
  -e CGT_XMX="${CGT_XMX:-3G}" \
  -e CGT_XMS="${CGT_XMS:-1G}" \
  -e CGT_MODID_FILTER="${CGT_MODID_FILTER:-}" \
  -e CGT_EXCLUDE_MODS="${CGT_EXCLUDE_MODS:-}" \
  -e CGT_TIMEOUT="${CGT_TIMEOUT:-1800}" \
  -e GRADLE_USER_HOME=/work/gradle-home \
  -e "CGT_ARGS=$*" \
  hearthwind-client-gametest \
  bash -lc 'bash /work/repo/custom-mods/tools/run_client_gametests.sh $CGT_ARGS'
RC=$?
set -e

# Outputs were written through the bind mount, so they are already on the
# host inside the stage - a plain host-side copy, no Docker API involved.
mkdir -p "$REPO/.tmp/shots/cgt" "$REPO/.tmp/logs"
cp -R "$STAGE/repo/.tmp/shots/cgt/." "$REPO/.tmp/shots/cgt/" 2>/dev/null || true
cp "$STAGE/repo/custom-mods/.tmp/logs/cgt-client.log" \
   "$REPO/.tmp/logs/cgt-client-container.log" 2>/dev/null || true
echo "screenshots copied back: $(ls "$REPO"/.tmp/shots/cgt/*.png 2>/dev/null | wc -l | tr -d ' ')"
exit "$RC"
