#!/usr/bin/env bash
# Runs the client gametest suite inside a pinned linux container (java 25 +
# xvfb + Mesa software GL) so UI testing never touches the host desktop or
# moves the user's mouse. Same run_client_gametests.sh + CGT_ENV=ci as CI:
# vanilla artifacts are provisioned from piston-meta into
# custom-mods/.tmp/cgt-provision (cached) and mods are staged from the pack
# dist plus the freshly built hearthwind-*/smallships jars.
#
# NOTE: this reuses HOST-built jars + host gradle cache (mounted read-only);
# run ./gradlew build on the host first. Building inside the container is
# deliberately avoided - host/container build outputs on one bind mount are
# not safely mixable (jar-manifest rewrites fail).
#
# Usage: bash tools/run_client_gametests_docker.sh [--keep-dir]
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$DIR/../.." && pwd)"

# Docker here is Colima: its QEMU VM holds 8 GB of RAM for as long as it runs
# and its leftovers reached 29 GB. Track whether the VM was already up and
# clean up on exit so a test run cannot leave the machine memory-starved
# (see tools/cleanup_container_tests.sh). CGT_KEEP=1 skips cleanup.
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

docker build -f "$DIR/docker/client-gametest.Dockerfile" -t hearthwind-client-gametest "$DIR/docker"

docker run --rm \
  -v "$REPO":/work/repo \
  -w /work/repo/custom-mods \
  -e CGT_ENV=ci \
  -e CGT_XVFB=1 \
  -e CGT_ARGS="$*" \
  hearthwind-client-gametest \
  bash -lc 'bash tools/run_client_gametests.sh $CGT_ARGS'
