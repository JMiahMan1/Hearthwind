#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUT_DIR="$ROOT_DIR/conversion/vendored"
OUT_JAR="$OUT_DIR/medieval_buildings-fabric-1.2.0+26.2.jar"

# Upstream 1.2.0 for MC 26.1.2; our 26.2 patch is metadata-only
# (fabric.mod.json version suffix + minecraft depends bump; zero class changes).
UPSTREAM_URL="https://cdn.modrinth.com/data/sc9lpPiU/versions/zRkAxe1q/medieval_buildings-fabric-26.1.2-1.2.0.jar"

echo "== [contrib] building medieval_buildings 1.2.0+26.2 =="
mkdir -p "$OUT_DIR"
python3 "$ROOT_DIR/contrib/port_tools/patched_rebuild.py" \
    "$UPSTREAM_URL" "$OUT_JAR" "$SCRIPT_DIR/patches/26.2-port.patch"
echo "  -> $OUT_JAR"
