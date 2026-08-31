#!/usr/bin/env bash
# Build script for YUNG suite ported mods for Hearthwind 26.2.
set -euo pipefail

REPO="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$REPO/.tmp/yungs-api"
mkdir -p "$TMP" "$REPO/conversion/vendored"

MODULES=(
    "YUNGs-API"
    "YUNGs-Better-Desert-Temples"
    "YUNGs-Better-End-Island"
    "YUNGs-Better-Fortresses"
    "YUNGs-Better-Jungle-Temples"
    "YUNGs-Better-Ocean-Monuments"
)

for MOD in "${MODULES[@]}"; do
    echo "=== Building $MOD ==="
    MOD_DIR="$TMP/$MOD"
    if [ ! -d "$MOD_DIR" ]; then
        echo "Cloning YUNG-GANG/$MOD (branch 26.2)..."
        git clone --branch 26.2 --depth 1 "https://github.com/YUNG-GANG/$MOD.git" "$MOD_DIR" 2>/dev/null || \
        git clone --depth 1 "https://github.com/YUNG-GANG/$MOD.git" "$MOD_DIR"
        if [ -f "$REPO/contrib/yungs/patches/$MOD.patch" ] && [ -s "$REPO/contrib/yungs/patches/$MOD.patch" ]; then
            echo "Applying patch for $MOD..."
            git -C "$MOD_DIR" apply "$REPO/contrib/yungs/patches/$MOD.patch" || true
        fi
    fi
    (cd "$MOD_DIR" && ./gradlew :Fabric:build --no-daemon -x test)
    find "$MOD_DIR/Fabric/build/libs" -name "*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" -exec cp {} "$REPO/conversion/vendored/" \;
done

echo "=== YUNG Suite Built Successfully into conversion/vendored/ ==="
