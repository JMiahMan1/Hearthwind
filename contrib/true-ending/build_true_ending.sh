#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUT_DIR="$ROOT_DIR/conversion/vendored"
OUT_JAR="$OUT_DIR/tru.e-ending-1.1.4d+26.2.jar"

# Upstream 1.1.4d is a datapack-style resource mod (depends only on
# fabric-resource-loader-v0) and is already MC-version-agnostic: our 26.2
# vendored jar is byte-identical to upstream, so the port is a pure
# repackage under the +26.2 name (no patch, no class changes).
UPSTREAM_URL="https://cdn.modrinth.com/data/MCnBYP0b/versions/BWvn4Jtr/tru.e-ending-1.1.4d.jar"

echo "== [contrib] building true-ending 1.1.4d+26.2 =="
mkdir -p "$OUT_DIR"
python3 "$ROOT_DIR/contrib/port_tools/patched_rebuild.py" \
    "$UPSTREAM_URL" "$OUT_JAR" --no-patch
echo "  -> $OUT_JAR"
