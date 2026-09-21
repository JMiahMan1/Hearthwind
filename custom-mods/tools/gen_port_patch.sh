#!/usr/bin/env bash
# Generates letsdo-<mod>/patches/26.2-port.patch: curated header (counts +
# added/removed lists) plus full unified diff against pristine upstream.
# Binaries are listed, not embedded (matches house convention).
#
# Usage: bash tools/gen_port_patch.sh <moddir> <upstream-dir> "<upstream-desc>"
#   e.g. bash tools/gen_port_patch.sh letsdo-brewery \
#     /path/to/upstream/Brewery "satisfyu/Brewery mod_version=2.1.9 (MC 1.21.1)"
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$DIR/.." && pwd)"
MOD="$1"; UPSTREAM="$2"; UPSTREAM_DESC="$3"
SRC="$ROOT/$MOD/src/main"
WORK="$ROOT/.tmp/pristine-$MOD"
PATCH="$ROOT/$MOD/patches/26.2-port.patch"

echo "== merging pristine $MOD"
rm -rf "$WORK"
mkdir -p "$WORK"
cp -r "$UPSTREAM/common/src/main/java" "$WORK/java"
cp -r "$UPSTREAM/common/src/main/resources" "$WORK/resources"
# fabric loader sources live one level deeper (net/net quirk): merge contents
if [ -d "$UPSTREAM/fabric/src/main/java/net" ]; then
  cp -rf "$UPSTREAM/fabric/src/main/java/." "$WORK/java/"
fi
if [ -d "$UPSTREAM/fabric/src/main/resources" ]; then
  cp -rn "$UPSTREAM/fabric/src/main/resources/." "$WORK/resources/" 2>/dev/null || true
fi

echo "== diffing"
CHANGED=$(diff -r -q "$WORK/java" "$SRC/java" 2>/dev/null | grep -c "^Files" || true)
CHANGED_RES=$(diff -r -q "$WORK/resources" "$SRC/resources" 2>/dev/null | grep -c "^Files" || true)
ONLY=$(diff -r -q "$WORK" "$SRC" 2>/dev/null | grep "^Only" || true)
ADDED=$(echo "$ONLY" | grep -c "Only in $SRC" || true)
REMOVED=$(echo "$ONLY" | grep -c "Only in $WORK" || true)
MODIFIED=$((CHANGED + CHANGED_RES))
BINARIES=$(diff -r -q "$WORK" "$SRC" 2>/dev/null | grep "^Files" | grep -icE "\.png|\.ogg|\.jar|\.icns" || true)

mkdir -p "$(dirname "$PATCH")"
{
  echo "# $MOD 26.2 port patch"
  echo "# Upstream: $UPSTREAM_DESC -> Hearthwind 26.2 Fabric-only port"
  echo "# Files: $MODIFIED modified, $ADDED added, $REMOVED removed (incl. NeoForge loader, not ported), $BINARIES binaries changed (listed, not embedded)"
  echo "# Added files:"
  echo "$ONLY" | grep "Only in $SRC" | sed -E "s|Only in $SRC/||; s|^|#   + |" | sort
  echo "# Removed files:"
  echo "$ONLY" | grep "Only in $WORK" | sed -E "s|Only in $WORK/||; s|^|#   - |" | sort
  echo "# Binary files (listed, content not embedded):"
  diff -r -q "$WORK" "$SRC" 2>/dev/null | grep "^Files" | grep -iE "\.png|\.ogg|\.jar|\.icns" | sed -E "s|^|#   ~ |" | sort || true
  echo ""
  echo "--- unified diff (text files only) ---"
  diff -r -u --exclude="*.png" --exclude="*.ogg" --exclude="*.jar" --exclude="*.icns" \
    --label="a/$MOD" --label="b/$MOD" "$WORK" "$SRC" || true
} > "$PATCH"
echo "wrote $PATCH ($(wc -l < "$PATCH") lines)"
