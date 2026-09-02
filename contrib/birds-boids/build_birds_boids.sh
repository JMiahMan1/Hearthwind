#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUT_DIR="$ROOT_DIR/conversion/vendored"
OUT_JAR="$OUT_DIR/birds-boids-fabric-1.3.1+26.2.jar"

# Upstream 1.3.1 for MC 26.1; our 26.2 patch is metadata-only
# (fabric.mod.json version suffix + minecraft depends bump).
UPSTREAM_URL="https://cdn.modrinth.com/data/CvX6rOtB/versions/4qQsi4jV/birds-boids-1.3.1%2B26.1.jar"

echo "== [contrib] building birds-boids 1.3.1+26.2 =="
mkdir -p "$OUT_DIR"
python3 "$ROOT_DIR/contrib/port_tools/patched_rebuild.py" \
    "$UPSTREAM_URL" "$OUT_JAR" "$SCRIPT_DIR/patches/26.2-port.patch"
echo "  -> $OUT_JAR"
