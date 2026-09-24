#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STAGE="${CGT_STAGE_DIR:-$ROOT/.tmp/fresh-world-smoke}"
SERVER_SRC="$ROOT/conversion/build/dist/server"
FABRIC_JAR="$STAGE/fabric-server.jar"
JAVA_BIN="${JAVA:-java}"
PORT="${WORLDGEN_PORT:-25567}"
RCON_PORT="${WORLDGEN_RCON_PORT:-25576}"
TIMEOUT_SECONDS="${WORLDGEN_TIMEOUT_SECONDS:-300}"
MINECRAFT_VERSION="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["targets"]["minecraft"])' "$ROOT/conversion/build.conf.json")"
FABRIC_LOADER_VERSION="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["targets"]["loader_version"])' "$ROOT/conversion/build.conf.json")"
FABRIC_URL="https://meta.fabricmc.net/v2/versions/loader/$MINECRAFT_VERSION/$FABRIC_LOADER_VERSION/1.1.0/server/jar"

[ -d "$SERVER_SRC/mods" ] || { printf 'missing generated server mods: %s\n' "$SERVER_SRC/mods" >&2; exit 1; }
command -v "$JAVA_BIN" >/dev/null 2>&1 || { printf 'java executable not found: %s\n' "$JAVA_BIN" >&2; exit 1; }

rm -rf "$STAGE"
mkdir -p "$STAGE"
curl -fsSL --retry 3 "$FABRIC_URL" -o "$FABRIC_JAR"
cp -R "$SERVER_SRC/mods" "$STAGE/mods"
if [ -d "$SERVER_SRC/world" ]; then
  cp -R "$SERVER_SRC/world" "$STAGE/world"
fi
printf 'eula=true\n' > "$STAGE/eula.txt"
printf 'pause-when-empty-seconds=-1\nlevel-type=minecraft:normal\nonline-mode=false\nserver-port=%s\nview-distance=6\nsimulation-distance=6\nspawn-protection=0\nsync-chunk-writes=true\nenable-rcon=true\nrcon.port=%s\nrcon.password=agedtest\n' "$PORT" "$RCON_PORT" > "$STAGE/server.properties"

SERVER_LOG="$STAGE/server.log"
(
  cd "$STAGE"
  nohup "$JAVA_BIN" -Xmx2G -jar fabric-server.jar nogui > "$SERVER_LOG" 2>&1 < /dev/null &
  printf '%s' "$!" > "$STAGE/server.pid"
)

cleanup() {
  if [ -f "$STAGE/server.pid" ]; then
    pid=$(cat "$STAGE/server.pid")
    kill "$pid" 2>/dev/null || true
    for _ in 1 2 3 4 5; do
      kill -0 "$pid" 2>/dev/null || break
      sleep 1
    done
    kill -9 "$pid" 2>/dev/null || true
  fi
}
trap cleanup EXIT

started=$(date +%s)
while true; do
  if grep -q 'Done (' "$SERVER_LOG" 2>/dev/null; then
    break
  fi
  if ! kill -0 "$(cat "$STAGE/server.pid")" 2>/dev/null; then
    printf 'server exited before Done\n' >&2
    tail -n 100 "$SERVER_LOG" >&2
    exit 1
  fi
  elapsed=$(( $(date +%s) - started ))
  if [ "$elapsed" -ge "$TIMEOUT_SECONDS" ]; then
    printf 'fresh-world server did not reach Done within %ss\n' "$TIMEOUT_SECONDS" >&2
    tail -n 100 "$SERVER_LOG" >&2
    exit 1
  fi
  sleep 5
done

if ! python3 "$ROOT/custom-mods/tools/rcon.py" 127.0.0.1 "$RCON_PORT" agedtest --connect-wait 60 --timeout 120 'forceload add -64 -64 64 64' > "$STAGE/forceload.log"; then
  printf 'fresh-world RCON forceload probe failed\n' >&2
  tail -n 100 "$SERVER_LOG" >&2
  exit 1
fi
sleep 20

if grep -q 'Not saving partially generated broken chunk' "$SERVER_LOG"; then
  printf 'fresh-world smoke found a broken chunk\n' >&2
  tail -n 120 "$SERVER_LOG" >&2
  exit 1
fi
if grep -q 'Failed to load chunk' "$SERVER_LOG" || grep -q 'Error while generating chunk' "$SERVER_LOG"; then
  printf 'fresh-world smoke found a chunk generation exception\n' >&2
  tail -n 120 "$SERVER_LOG" >&2
  exit 1
fi
if ! grep -q 'Done (' "$SERVER_LOG"; then
  printf 'fresh-world smoke lost server readiness marker\n' >&2
  exit 1
fi

printf 'fresh-world smoke passed: Done, forced chunk grid generated, no broken-chunk errors\n'
printf 'log: %s\n' "$SERVER_LOG"
