#!/usr/bin/env bash
# Runs the client gametest suite inside a pinned linux container (java 26 +
# xvfb + Mesa software GL) so UI testing never touches the host desktop or
# moves the user's mouse. Uses the same run_client_gametests.sh as CI with
# CGT_ENV=ci: vanilla artifacts are provisioned from piston-meta into
# .tmp/cgt-provision (cached on the host repo) and mods are staged from the
# pack dist.
#
# First container run is slow (gradle + deps land in .tmp/docker-gradle).
#
# Usage: bash tools/run_client_gametests_docker.sh [--keep-dir]
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$DIR/../.." && pwd)"

docker build -f "$DIR/docker/client-gametest.Dockerfile" -t hearthwind-client-gametest "$DIR/docker"

mkdir -p "$REPO/.tmp/docker-gradle"
exec docker run --rm \
  -v "$REPO":/work/repo \
  -w /work/repo/custom-mods \
  -e GRADLE_USER_HOME=/work/repo/.tmp/docker-gradle \
  -e CGT_ENV=ci \
  -e CGT_XVFB=1 \
  hearthwind-client-gametest \
  bash tools/run_client_gametests.sh "$@"
