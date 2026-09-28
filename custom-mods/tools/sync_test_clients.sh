#!/usr/bin/env bash
# Rebuild sync: copy freshly built hearthwind module jars everywhere they run.
# Automatically ensures the background dev server is restarted and listening on port 25565.
# Usage:
#   bash tools/sync_test_clients.sh            # sync jars + ensure server running
#   bash tools/sync_test_clients.sh --no-server # sync jars without restarting server
set -euo pipefail

REPO="$(cd "$(dirname "$0")/../.." && pwd)"
DESTS=(
  "$REPO/dev-server/mods"
  "$REPO/dev-client/client/mods"
  "$HOME/Library/Application Support/PrismLauncher/instances/Hearthwind-Client/minecraft/mods"
  "$HOME/Library/Application Support/PrismLauncher/instances/Hearthwind-Server/minecraft/mods"
)

jars=$(find "$REPO/custom-mods" -path "*build/libs/*26.2*.jar" ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null)
# Third-party jars only. conversion/dist and conversion/build/dist also hold
# OLD copies of our own hearthwind-* jars (whatever build_pack.py last
# produced); they must never overwrite the fresh builds below.
vendored=$(find "$REPO/conversion/vendored" "$REPO/conversion/build/dist/server/mods" "$REPO/conversion/dist/server/mods" -name "*.jar" ! -name "hearthwind-*" 2>/dev/null || true)
[ -n "$jars" ] || { echo "no built jars found — run: cd custom-mods && ./gradlew build --no-daemon --max-workers=2"; exit 1; }

count=0
for jar in $vendored $jars; do
  name="$(basename "$jar")"
  for dest in "${DESTS[@]}"; do
    mkdir -p "$dest"
    cp "$jar" "$dest/$name"
  done
  echo "synced $name -> ${#DESTS[@]} destinations"
  count=$((count + 1))
done
echo "synced $count jars"

# Prune stale versions: a dest jar whose MOD ID matches a synced jar's mod id
# but has a different filename is an old version and must go (the loader
# picks one arbitrarily). Match by fabric.mod.json id, NEVER by filename
# prefix: sibling mods share prefixes ("dungeons-and-taverns-5.3.2" vs
# "dungeons-and-taverns-pillager-outpost-overhaul-v3.3", "chalk" vs
# "chalk-colorful-addon") and prefix pruning ate them.
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
printf '%s\n' $vendored $jars | python3 "$SCRIPT_DIR/prune_stale_jars.py" "${DESTS[@]}"

# Guard: every deployed hearthwind jar must match the build output byte for
# byte, so a stale copy can never silently win again.
for jar in $jars; do
  name="$(basename "$jar")"
  for dest in "${DESTS[@]}"; do
    cmp -s "$jar" "$dest/$name" || { echo "ERROR: $dest/$name is stale (differs from $jar)" >&2; exit 1; }
  done
done
echo "verified $(printf '%s\n' "$jars" | wc -l | tr -d ' ') hearthwind jars match the build output"

# Server health check and auto-restart
if [ "${1:-}" != "--no-server" ]; then
  pids=$(pgrep -f "[f]abric-server.jar" | grep -v 49171 || true)
  if [ -n "$pids" ]; then
    echo "restarting dev server (pids: $pids)..."
    kill $pids
    for _ in $(seq 1 30); do
      pgrep -f "[f]abric-server.jar" | grep -qv 49171 || break
      sleep 0.5
    done
  else
    echo "starting dev server..."
  fi

  python3 - "$REPO" <<'EOF'
import subprocess, sys, os

repo = sys.argv[1]
os.makedirs(os.path.join(repo, ".tmp/logs"), exist_ok=True)
log = open(os.path.join(repo, ".tmp/logs/server_run.log"), "a")
subprocess.Popen(
    ["/usr/local/opt/openjdk/bin/java", "-Xmx1536M", "-jar", "fabric-server.jar", "nogui"],
    stdout=log, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL,
    start_new_session=True, cwd=os.path.join(repo, "dev-server"),
)

rcon = os.path.join(repo, "custom-mods", "tools", "rcon.py")
rc = subprocess.call([sys.executable, rcon, "127.0.0.1", "25575", "agedtest",
                      "list", "--connect-wait", "120"])
if rc == 0:
    print("Dev server is LIVE (RCON ready => world fully loaded).")
else:
    print("Warning: server started in background, still warming up...")
    sys.exit(rc or 1)
EOF
fi
