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
TIMEOUT=${CGT_TIMEOUT:-1800}
PROGRESS_INTERVAL=${CGT_PROGRESS_INTERVAL:-300}
[ "$PROGRESS_INTERVAL" -gt 0 ] || PROGRESS_INTERVAL=300
KEEP=0
[ "${1:-}" = "--keep-dir" ] && KEEP=1

mkdir -p "$ROOT/.tmp/logs"
rm -rf "$ROOT/.tmp/shots/cgt"
mkdir -p "$ROOT/.tmp/shots/cgt"
rm -f "$LOG"
echo "== assembling client gametest game dir: $WORK"
rm -rf "$WORK"
mkdir -p "$WORK/mods"

# CI/Docker (CGT_ENV=ci) provision vanilla artifacts + stage mods from the pack dist;
# default (macOS dev host) reuses the launcher install + dev-client mod dir.
if [ "${CGT_ENV:-}" = "ci" ]; then
  PROV="$ROOT/.tmp/cgt-provision"
  echo "== provisioning vanilla client artifacts (piston-meta) -> $PROV"
  PROV_OUT=$(python3 "$DIR/provision_ci_client.py" --out "$PROV") || {
    echo "ERROR: provisioning failed" >&2
    exit 1
  }
  eval "$PROV_OUT"
  CLIENT_MODS="${CGT_MODS_SRC:-$REPO/conversion/build/dist/client/mods}"
else
  CLIENT_MODS="${CGT_MODS_SRC:-$REPO/dev-client/client/mods}"
fi
if [ ! -d "$CLIENT_MODS" ]; then
  echo "ERROR: mod source dir not found at $CLIENT_MODS" >&2
  exit 1
fi
cp "$CLIENT_MODS"/*.jar "$WORK/mods/"
# the FabricClientGameTestRunner entrypoint lives in the API module jar itself
GRADLE_CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/caches"
CGT_API=$(ls "$GRADLE_CACHE/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-client-gametest-api-v1/6.0.0+515ac5339e"/*/*.jar 2>/dev/null | grep -v sources | head -1)
if [ -z "$CGT_API" ]; then
  echo "ERROR: fabric-client-gametest-api-v1 jar not found in gradle cache" >&2
  exit 1
fi
cp "$CGT_API" "$WORK/mods/"
# Overlay EVERY locally built module (fresh source wins over CLIENT_MODS,
# which may hold stale dist jars - stale jars once masked real failures).
shopt -s nullglob
for j in "$ROOT"/*/build/libs/*26.2+0.1.0.jar; do
  [ -f "$j" ] || continue
  case "$j" in *sources*) continue ;; esac
  base="$(basename "$j")"; mod="${base%-26.2*}"
  rm -f "$WORK/mods/$mod"-*.jar
  cp "$j" "$WORK/mods/"
done
shopt -u nullglob
# One jar per fabric.mod.json id (prefer pack MC version, then higher).
# dist once shipped both boids-26.2 and a broken boids-26.3; raw cp kept both.
python3 - "$WORK/mods" <<'PY'
import json, sys, zipfile
from pathlib import Path
mods = Path(sys.argv[1])
mc = "26.2"
for cand in (
    Path("conversion/build.conf.json"),
    Path("../conversion/build.conf.json"),
    Path("../../conversion/build.conf.json"),
):
    if cand.exists():
        try:
            mc = json.loads(cand.read_text())["targets"]["minecraft"]
        except Exception:
            pass
        break

def score(ver: str):
    return (mc in ver, ver)

by_id = {}
for j in sorted(mods.glob("*.jar")):
    if j.name == "fabric-client-gametest-api-v1.jar" or j.name == "fabric-gametest-api-v1.jar":
        continue
    try:
        with zipfile.ZipFile(j) as z:
            fm = json.loads(z.read("fabric.mod.json"))
            mid = fm.get("id")
            ver = fm.get("version") or ""
    except Exception:
        continue
    if not mid:
        continue
    by_id.setdefault(mid, []).append((score(ver), j))
removed = 0
for mid, entries in by_id.items():
    if len(entries) < 2:
        continue
    entries.sort(key=lambda t: t[0])
    for _, j in entries[:-1]:
        j.unlink()
        removed += 1
if removed:
    print(f"deduped {removed} stale duplicate mod jars by id")
PY
echo "mods staged: $(ls "$WORK/mods" | wc -l | tr -d ' ')"

# Optional exclusion for isolating a misbehaving client mod from the harness
# (e.g. CGT_EXCLUDE_MODS=fancymenu). Matched by jar filename prefix.
if [ -n "${CGT_EXCLUDE_MODS:-}" ]; then
  for name in ${CGT_EXCLUDE_MODS//,/ }; do
    find "$WORK/mods" -maxdepth 1 -name "${name}*.jar" -delete
    echo "excluded mod jars matching ${name}*.jar"
  done
fi

# Apply idempotent vendored-jar content patches (kiwi 26.2 clinit flip) so a
# host/container run never boots unpatched bytes even if dist was rebuilt
# without build_pack.py.
python3 "$REPO/conversion/scripts/patch_vendored.py" "$REPO/conversion" || {
  echo "ERROR: patch_vendored.py failed" >&2
  exit 1
}
# Re-copy kiwi from vendored in case CLIENT_MODS held a stale unpatched jar.
if [ -f "$REPO/conversion/vendored/kiwi-26.0.20+fabric.jar" ]; then
  cp "$REPO/conversion/vendored/kiwi-26.0.20+fabric.jar" "$WORK/mods/"
fi
# true_ending / natures_spirit 26.2 data-schema patches (time_check clock,
# entity_type predicate). "$WORK/mods" is patched directly because staging
# copied dist before this point.
python3 "$REPO/conversion/scripts/patch_legacy_data.py" "$REPO/conversion" "$WORK/mods" || {
  echo "ERROR: patch_legacy_data.py failed" >&2
  exit 1
}

cat > "$WORK/options.txt" <<'EOF'
onboardAccessibility:false
pauseOnLostFocus:false
skipMultiplayerWarning:true
narrator:false
EOF

# kiwi 26.0.20 has no 26.2 build: cosmetic keybind reads Minecraft.screen
# (removed) on first client tick. Prewrite client config so ConfigHandler.init
# loads false BEFORE the first END_CLIENT_TICK (overrides are not staged here).
# Full key set matches Kiwi's rewriter so it cannot regenerate with true.
mkdir -p "$WORK/config"
cat > "$WORK/config/kiwi-client.yaml" <<'EOF'
# Hearthwind CGT: keep kiwi cosmetic screen keybind off on 26.2
---
contributorCosmetic: ''
cosmeticScreenKeybind: false
globalTooltip: false
noMicrosoftTelemetry: true
qol:
  noForceBackup: false
  suppressExperimentalSettingsWarning: false
  titleScreenNoFade: false
  hideDataComponentsTooltip: false
  loadingOverlayNoFade: false
  superClearChat: false
debug:
  showTranslatedTagsByDefault: false
  printDataComponentsWhenCopy: true
  tagsTooltip: true
  debugTooltipMsg: true
  tagsTooltipAppendKeybindHint: false
  F3CopyInInventory: true
  tagsPerPage: 6
EOF

# Pre-agree the EULA for the gametest dedicated server. It runs with the
# JVM's process cwd (NOT --gameDir), and owo-lib's Eula mixin falls back to
# an interactive System.in prompt which the framework's 10s server-start
# watchdog times out - so write eula.txt to BOTH places.
printf 'eula=true\n' > "$WORK/eula.txt"
printf 'eula=true\n' > ./eula.txt

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

# Colima/CI VMs have ~8 GB total; default to a safer pair and allow override.
VMARGS=(-Xmx"${CGT_XMX:-3G}" "-Xms${CGT_XMS:-1G}" "-XX:+UseG1GC" "-XX:MaxGCPauseMillis=50" "-XX:G1HeapRegionSize=8M" "--enable-native-access=ALL-UNNAMED" "--sun-misc-unsafe-memory-access=allow"
  "-Dfabric.gameJarPath=$GAME_JAR" "-Dfabric.client.gametest"
  "-Dfabric.client.gametest.screenshotDir=$WORK/screenshots")
case "$(uname)" in
  Darwin) VMARGS+=("-XstartOnFirstThread") ;;
esac

# Optional comma-separated mod-id filter, e.g.
# CGT_MODID_FILTER=hearthwind_client,dungeonz to skip the chipped entrypoint
# while isolating a failure. Maps to the API's own modid filter.
if [ -n "${CGT_MODID_FILTER:-}" ]; then
  VMARGS+=("-Dfabric.client.gametest.modid=$CGT_MODID_FILTER")
fi

CMD=(java "${VMARGS[@]}" -cp "$LOADER:$MIXIN:$MIXEX:$ASM$MCCP"
  net.fabricmc.loader.impl.launch.knot.KnotClient
  --username TestPlayer --version 26.2 --gameDir "$WORK"
  --assetsDir "${CGT_ASSETS_DIR:-$HOME/Library/Application Support/minecraft/assets}"
  --assetIndex "$ASSET_IDX" --uuid 00000000-0000-0000-0000-000000000000
  --accessToken 0 --versionType Hearthwind)

LAUNCH=()
if [ "${CGT_XVFB:-0}" = "1" ]; then
  LAUNCH=(xvfb-run -a -s "-screen 0 1280x800x24")
  export LIBGL_ALWAYS_SOFTWARE=1
fi

# Pre-create the default world save dir: graphlib's onCreate fires during
# ChunkMap.<init> (before the framework lazily creates the world dir) and
# crashes with NoSuchFileException if it is absent.
mkdir -p "$WORK/saves/New World"

echo "== launching client gametest run (log: $LOG)"
: > "$LOG"
nohup "${LAUNCH[@]}" "${CMD[@]}" > "$LOG" 2>&1 < /dev/null &
CPID=$!

ELAPSED=0
NEXT_PROGRESS=$PROGRESS_INTERVAL
while kill -0 "$CPID" 2>/dev/null && [ "$ELAPSED" -lt "$TIMEOUT" ]; do
  sleep 5
  ELAPSED=$((ELAPSED + 5))
  if [ "$ELAPSED" -ge "$NEXT_PROGRESS" ]; then
    LAST=$(tail -1 "$LOG" 2>/dev/null || true)
    SHOTS_NOW=$(find "$WORK/screenshots" -maxdepth 1 -name '*.png' 2>/dev/null | wc -l | tr -d ' ')
    printf 'client progress: elapsed=%ss/%ss screenshots=%s last=%s\n' "$ELAPSED" "$TIMEOUT" "$SHOTS_NOW" "$LAST"
    NEXT_PROGRESS=$((NEXT_PROGRESS + PROGRESS_INTERVAL))
  fi
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
if grep -qE "Exception in thread|GameTest.*(failed|FAILED)|Minecraft has crashed" "$LOG"; then
  echo "FAIL: exceptions/failures/crash found in log:"
  grep -E "Minecraft has crashed|^Caused by:|GameTest.*(failed|FAILED)" "$LOG" | head -6
  RC=1
fi
# Missing-texture gate scoped to OUR content only: upstream mods ship their
# own broken refs (e.g. natures_spirit pizza) which we neither own nor fix.
OUR_NS="meadow|bakery|brewery|candlelight|farm_and_charm|herbalbrews|vinery|nethervinery|smallships|dehydration|environmentz|hearthwind|hearthwind_survival|hearthwind_world|hearthwind_primitive|hearthwind_jobs|hearthwind_skills|hearthwind_client|earlystage|agedaddition|levelz|naturalist|adventurez|antiqueatlas|exposure|inmis"
OUR_MISSING=$(grep -E "Missing textures? in model|Missing texture references in model" "$LOG" \
  | sed 's/.*WARN]: //' | grep -E "($OUR_NS):" | sort -u)
if [ -n "$OUR_MISSING" ]; then
  echo "FAIL: client reported missing textures in OUR content:"
  echo "$OUR_MISSING" | head -20
  RC=1
fi
# Datapack parse/tag debt gate. The runner tolerates these, so the final7
# run passed while adventurez/fleshz/true_ending/natures_spirit JSON still
# failed 26.2 codecs. Any recurrence in our or vendored namespaces is a FAIL.
PARSE_BAD=$(grep -E "Couldn't parse data file '($OUR_NS|minecraft|adventurez|fleshz|true_ending|natures_spirit):" "$LOG" | sort -u)
if [ -n "$PARSE_BAD" ]; then
  echo "FAIL: datapack parse errors in our/vendored namespaces:"
  echo "$PARSE_BAD" | sed "s/.*Couldn't parse data file '//; s/'.*//" | sort | uniq -c | sort -rn | head -20
  RC=1
fi
TAG_BAD=$(grep -E "Couldn't load tag .*missing following references:.*#(aged|earlystage):" "$LOG" | sort -u)
if [ -n "$TAG_BAD" ]; then
  echo "FAIL: missing #aged/#earlystage tag references:"
  echo "$TAG_BAD" | head -10
  RC=1
fi
# PackServerConnectGameTests MUST stay the LAST fabric-client-gametest
# entrypoint: closing its in-process dedicated server exits the whole JVM
# (exit 0), so any test registered after it silently never runs. Its
# screenshot existing therefore proves every earlier test ran.
if [ "$SHOTS" -lt 1 ] || [ -z "$(find "$WORK/screenshots" -name '*_pack_server_gate_sync.png' -print -quit)" ]; then
  echo "FAIL: expected pack_server_gate_sync screenshot missing"
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
