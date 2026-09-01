#!/usr/bin/env bash
# Runs the client gametest suite inside a pinned linux container (java 26 +
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

docker build -f "$DIR/docker/client-gametest.Dockerfile" -t hearthwind-client-gametest "$DIR/docker"

exec docker run --rm \
  -v "$REPO":/work/repo \
  -v "$HOME/.gradle:/root/.gradle:ro" \
  -w /work/repo/custom-mods \
  -e CGT_ENV=ci \
  -e CGT_XVFB=1 \
  -e CGT_ARGS="$*" \
  hearthwind-client-gametest \
  bash -lc 'bash tools/run_client_gametests.sh $CGT_ARGS'
