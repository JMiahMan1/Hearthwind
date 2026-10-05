#!/usr/bin/env bash
# Containerized server gametests (MANDATORY path: never run game tests on the host).
# Host builds first (./gradlew build), then this stages jars + data and runs
# the standard harness inside the pinned linux image (java 25).
# Inputs are BIND-MOUNTED from the stage; no named volume and no `docker cp`,
# so a jar removed from the staged dist is gone from the container in the
# same run that removed it.
#
# Usage: bash tools/run_gametests_container.sh [--keep-server]
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
docker build -f "$DIR/docker/server-gametest.Dockerfile" -t hearthwind-server-gametest "$DIR/docker"

docker run --rm \
  -v "$STAGE:/work" \
  -w /work/repo/custom-mods \
  -e SKIP_BUILD=1 \
  -e JAVA_HOME=/opt/java/openjdk \
  -e GRADLE_USER_HOME=/work/gradle-home \
  -e "CGT_SRV_ARGS=$*" \
  hearthwind-server-gametest \
  bash -lc 'bash /work/repo/custom-mods/tools/run_gametests.sh $CGT_SRV_ARGS'
