#!/usr/bin/env bash
# Runs the fabric-client-gametest-api-v1 tests shipped in hearthwind-client.
# Boots a REAL client with -Dfabric.client.gametest in a throwaway game dir,
# waits for the FabricClientGameTestRunner to finish, then verifies the exit
# code and that every expected screenshot was produced. No mouse/keyboard
# automation is involved, so this is safe on a desktop (and under xvfb in CI).
#
# Usage: bash tools/run_client_gametests.sh [--keep-dir]
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"        # custom-mods/tools
ROOT="$(cd "$DIR/.." && pwd)"                              # custom-mods
REPO="$(cd "$ROOT/.." && pwd)"
WORK="$ROOT/.tmp/cgt-game"
LOG="$ROOT/.tmp/logs/cgt-client.log"
TIMEOUT=${CGT_TIMEOUT:-900}
KEEP=0
[ "${1:-}" = "--keep-dir" ] && KEEP=1

mkdir -p "$ROOT/.tmp/logs"
echo "== assembling client gametest game dir: $WORK"
rm -rf "$WORK"
mkdir -p "$WORK/mods"

CLIENT_MODS="$REPO/dev-client/client/mods"
if [ ! -d "$CLIENT_MODS" ]; then
  echo "ERROR: mod source dir not found at $CLIENT_MODS" >&2
  exit 1
fi
cp "$CLIENT_MODS"/*.jar "$WORK/mods/"
# the FabricClientGameTestRunner entrypoint lives in the API module jar itself
CGT_API=$(ls "$HOME/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-client-gametest-api-v1/6.0.0+515ac5339e"/*/*.jar 2>/dev/null | grep -v sources | head -1)
if [ -z "$CGT_API" ]; then
  echo "ERROR: fabric-client-gametest-api-v1 jar not found in gradle cache" >&2
  exit 1
fi
cp "$CGT_API" "$WORK/mods/"
for j in "$ROOT"/hearthwind-*/build/libs/*26.2+0.1.0.jar; do
  [ -f "$j" ] || continue
  case "$j" in *sources*) continue ;; esac
  cp "$j" "$WORK/mods/"
done
echo "mods staged: $(ls "$WORK/mods" | wc -l | tr -d ' ')"

cat > "$WORK/options.txt" <<'EOF'
onboardAccessibility:false
pauseOnLostFocus:false
skipMultiplayerWarning:true
EOF

echo "== resolving minecraft classpath"
CPINFO=$(CGT_TOOLS="$DIR" python3 - <<'PY'
import os, sys
sys.path.insert(0, os.environ["CGT_TOOLS"])
from client_harness import build_classpath
loader, mixin, mixex, asm, mccp, game_jar, asset_idx = build_classpath()
print(f"{loader}\n{mixin}\n{mixex}\n{asm}\n{mccp}\n{game_jar}\n{asset_idx}")
PY
) || { echo "ERROR: classpath build failed" >&2; exit 1; }
{ read -r LOADER; read -r MIXIN; read -r MIXEX; read -r ASM; read -r MCCP; read -r GAME_JAR; read -r ASSET_IDX; } <<< "$CPINFO"

VMARGS=(-Xmx3G "--enable-native-access=ALL-UNNAMED" "--sun-misc-unsafe-memory-access=allow"
  "-Dfabric.gameJarPath=$GAME_JAR" "-Dfabric.client.gametest"
  "-Dfabric.client.gametest.screenshotDir=$WORK/screenshots")
case "$(uname)" in
  Darwin) VMARGS+=("-XstartOnFirstThread") ;;
esac

CMD=(java "${VMARGS[@]}" -cp "$LOADER:$MIXIN:$MIXEX:$ASM$MCCP"
  net.fabricmc.loader.impl.launch.knot.KnotClient
  --username TestPlayer --version 26.2 --gameDir "$WORK"
  --assetsDir "$HOME/Library/Application Support/minecraft/assets"
  --assetIndex "$ASSET_IDX" --uuid 00000000-0000-0000-0000-000000000000
  --accessToken 0 --versionType Hearthwind)

echo "== launching client gametest run (log: $LOG)"
: > "$LOG"
nohup "${CMD[@]}" > "$LOG" 2>&1 < /dev/null &
CPID=$!

ELAPSED=0
while kill -0 "$CPID" 2>/dev/null && [ "$ELAPSED" -lt "$TIMEOUT" ]; do
  sleep 5
  ELAPSED=$((ELAPSED + 5))
done

if kill -0 "$CPID" 2>/dev/null; then
  echo "ERROR: client gametest run did not finish within ${TIMEOUT}s - killing" >&2
  kill -TERM "$CPID" 2>/dev/null
  sleep 3
  kill -9 "$CPID" 2>/dev/null
  RC=1
else
  wait "$CPID" 2>/dev/null
  RC=$?
fi

echo "== last 25 log lines:"
tail -25 "$LOG" || true
echo "== verdict"
grep -c "FabricClientGameTest" "$LOG" 2>/dev/null | sed 's/^/runner mentions: /'
SHOTS=$(ls "$WORK/screenshots"/*.png 2>/dev/null | wc -l | tr -d ' ')
echo "screenshots: $SHOTS in $WORK/screenshots"
ls -l "$WORK/screenshots" 2>/dev/null || true
if grep -qE "Exception in thread|GameTest.*(failed|FAILED)" "$LOG"; then
  echo "FAIL: exceptions/failures found in log"
  RC=1
fi
if [ "$SHOTS" -lt 1 ]; then
  echo "FAIL: no screenshots produced"
  RC=1
fi
if [ "$RC" -eq 0 ]; then
  echo "client gametests: PASS (exit 0, screenshots present)"
else
  echo "client gametests: FAIL (exit $RC)"
fi
if [ "$SHOTS" -ge 1 ]; then
  mkdir -p "$REPO/.tmp/shots/cgt"
  cp "$WORK"/screenshots/*.png "$REPO/.tmp/shots/cgt/" 2>/dev/null
  echo "screenshots preserved in .tmp/shots/cgt/"
fi
if [ "$RC" -eq 0 ] && [ "$KEEP" -eq 0 ]; then
  rm -rf "$WORK"
else
  echo "game dir kept for inspection: $WORK"
fi
exit "$RC"
