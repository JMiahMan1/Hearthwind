#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUT_DIR="$ROOT_DIR/conversion/vendored"
OUT_JAR="$OUT_DIR/tlc-fabric-2.1.1+26.2.jar"

# Upstream 2.1.1 for MC 26.1. Our 26.2 patch:
# - fabric.mod.json version suffix + minecraft depends bump
# - TLCProcessors: StructureProcessor registration changed from the old
#   codec-lambda form to a direct MapCodec reference registered in
#   BuiltInRegistries.STRUCTURE_PROCESSOR
# - FoundationProcessor: implements StructureProcessor with mapCodec()
# The compiled classes are carried INSIDE the binary patch, so a plain
# `git apply --binary` reproduces the full port; no upstream build needed.
UPSTREAM_URL="https://cdn.modrinth.com/data/FGlHZl7X/versions/ii7tzK3s/tlc-fabric-26.1-2.1.1.jar"

echo "== [contrib] building the lost castle 2.1.1+26.2 =="
mkdir -p "$OUT_DIR"
python3 "$ROOT_DIR/contrib/port_tools/patched_rebuild.py" \
    "$UPSTREAM_URL" "$OUT_JAR" "$SCRIPT_DIR/patches/26.2-port.patch"
echo "  -> $OUT_JAR"
