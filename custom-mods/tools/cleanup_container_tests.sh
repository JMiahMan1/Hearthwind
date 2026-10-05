#!/usr/bin/env bash
# Remove everything the containerised Hearthwind tests create.
#
# The tests run in Docker via Colima on this host. Every run used to leave
# behind the Hearthwind images (~1.5 GB), the seeded volumes (cgtvol alone is
# ~1.6 GB) and a running QEMU VM that holds 8 GB of RAM indefinitely. That is
# not acceptable on a 32 GB laptop: an 8 GB VM left running for a day drove
# the machine into swap exhaustion, and the Minecraft client then died with
# SIGSEGV inside the C2 compiler (Chunk::next_chop on a garbage pointer) purely
# because native allocation could not be satisfied. The crashes at
# hs_err_pid22780 / pid58980 both happened while that VM was resident.
#
# Both container wrappers call this from an EXIT trap, so a normal run cleans
# up after itself. Set CGT_KEEP=1 to keep images and volumes for a debugging
# session; the VM is only stopped when the wrapper started it.
#
# Usage: bash tools/cleanup_container_tests.sh [--stop-vm] [--keep-images]
set -uo pipefail

DOCKER_HOST_DEFAULT="unix://$HOME/.colima/default/docker.sock"
export DOCKER_HOST="${DOCKER_HOST:-$DOCKER_HOST_DEFAULT}"

STOP_VM=0
KEEP_IMAGES=0
for a in "$@"; do
  case "$a" in
    --stop-vm)     STOP_VM=1 ;;
    --keep-images) KEEP_IMAGES=1 ;;
  esac
done

say() { printf '  %s\n' "$*"; }

if ! docker info >/dev/null 2>&1; then
  say "docker not reachable - nothing to clean inside the VM"
  if [ "$STOP_VM" = 1 ] && command -v colima >/dev/null 2>&1; then
    colima status >/dev/null 2>&1 && { colima stop >/dev/null 2>&1; say "colima stopped"; }
  fi
  exit 0
fi

# Containers first: a stopped container can hold a volume open.
for c in $(docker ps -aq --filter "name=cgt" --filter "name=hearthwind" 2>/dev/null); do
  docker rm -f "$c" >/dev/null 2>&1 && say "removed container $c"
done

# Volumes the tests seed. cgtvol is the big one (~1.6 GB); the others are
# per-test scratch created by individual suites.
for v in cgtvol cgtexposure cgtcookingvol2 cgtcookingvol cgtfoodvol; do
  if docker volume inspect "$v" >/dev/null 2>&1; then
    docker volume rm -f "$v" >/dev/null 2>&1 && say "removed volume $v"
  fi
done

if [ "$KEEP_IMAGES" = 0 ]; then
  for i in hearthwind-client-gametest:latest hearthwind-server-gametest:latest; do
    docker rmi -f "$i" >/dev/null 2>&1 && say "removed image $i"
  done
fi

if [ "$STOP_VM" = 1 ] && command -v colima >/dev/null 2>&1; then
  if colima status >/dev/null 2>&1; then
    colima stop >/dev/null 2>&1 && say "colima stopped (frees the VM's RAM)"
  fi
fi

say "container test cleanup done"
