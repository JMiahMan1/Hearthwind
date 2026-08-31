#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUT_DIR="$ROOT_DIR/conversion/vendored"
OUT_JAR="$OUT_DIR/tru.e-ending-1.1.4d+26.2.jar"

echo "== [contrib] building true-ending 1.1.4d+26.2 =="
mkdir -p "$OUT_DIR"
if [ -f "$OUT_JAR" ]; then
    echo "  -> $OUT_JAR already built and ready"
fi
