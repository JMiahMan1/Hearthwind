#!/usr/bin/env bash
# Run a local Hearthwind dev server (server-side, vanilla-joinable)
# One command: bash tools/run_local_server.sh [--build]
# --build  rebuild mods + resolve deps + materialize server before launch
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
JAVA_CANDIDATE="$HOME/Library/Application Support/PrismLauncher/java/java-runtime-epsilon/jre.bundle/Contents/Home/bin/java"
if [ -x "$JAVA_CANDIDATE" ]; then
  JAVA="$JAVA_CANDIDATE"
else
  JAVA="$(which java 2>/dev/null || echo java)"
fi

if [ "${1:-}" = "--build" ]; then
  echo "== --build: resolving deps + building mods =="
  python3 "$ROOT/conversion/scripts/resolve_deps.py" || echo "resolve_deps: some deps missing for 26.2 (expected 41/57 ok, watchlist YUNG etc.) - continuing with available"
  python3 "$ROOT/conversion/scripts/build_pack.py" --server-dir
  (cd "$ROOT/custom-mods" && "$JAVA" -version 2>&1 | head -n 1; ./gradlew build --no-daemon --max-workers=2 -q)
fi

# Setup dev-server if missing or stale
if [ ! -d "$ROOT/dev-server/mods" ]; then
  echo "== dev-server mods missing - running setup =="
  bash "$ROOT/custom-mods/tools/setup_prism_dev.sh" 2>&1 | tail -n 20
fi

# Ensure latest custom jars are in dev-server
for f in "$ROOT"/custom-mods/hearthwind-*/build/libs/*26.2+0.1.0.jar; do
  [[ -f "$f" ]] || continue
  [[ "$f" == *"-sources.jar" ]] && continue
  [[ "$f" == *"hearthwind-client"* ]] && continue
  cp "$f" "$ROOT/dev-server/mods/" 2>/dev/null || true
done

if [ ! -f "$ROOT/dev-server/fabric-server.jar" ]; then
  echo "== Fetching fabric-server.jar 0.19.3 for 26.2 =="
  curl -sL -o "$ROOT/dev-server/fabric-server.jar" "https://meta.fabricmc.net/v2/versions/loader/26.2/0.19.3/1.1.0/server/jar"
fi

echo "eula=true" > "$ROOT/dev-server/eula.txt"
cat > "$ROOT/dev-server/server.properties" <<'PROPS'
pause-when-empty-seconds=-1
enable-rcon=true
rcon.port=25575
rcon.password=agedtest
view-distance=6
simulation-distance=6
level-type=minecraft:normal
online-mode=false
server-port=25565
PROPS

# Kill old
pkill -f "dev-server/fabric-server.jar" 2>/dev/null || true
pkill -f "fabric-server.jar.*dev-server" 2>/dev/null || true
sleep 1
rm -f "$ROOT/dev-server/world/session.lock" 2>/dev/null || true

echo ""
echo "== Starting Hearthwind dev server =="
echo "  Java: $JAVA"
"$JAVA" -version 2>&1 | head -n 1
echo "  Mods: $(ls "$ROOT/dev-server/mods"/*.jar 2>/dev/null | wc -l) jars (hearthwind: $(ls "$ROOT/dev-server/mods"/hearthwind*.jar 2>/dev/null | wc -l))"
echo "  Logs: tail -f dev-server/boot.log  (or dev-server/logs/latest.log)"
echo "  Connect: localhost:25565  (Prism Hearthwind-Dev-Client or vanilla 26.2)"
echo "  RCON: python3 custom-mods/tools/rcon.py 127.0.0.1 25575 agedtest \"list\""
echo "  Stop: Ctrl+C or RCON \"stop\" or pkill -f fabric-server"
echo ""

HEAP="${HEAP:-3G}"
TIMEOUT_CMD="$(which timeout 2>/dev/null || which gtimeout 2>/dev/null || echo timeout)"
# Use timeout if available, else just run
if command -v timeout >/dev/null 2>&1; then
  (cd "$ROOT/dev-server" && exec timeout 3600 "$JAVA" -Xmx"$HEAP" -jar fabric-server.jar nogui)
else
  (cd "$ROOT/dev-server" && exec "$JAVA" -Xmx"$HEAP" -jar fabric-server.jar nogui)
fi
