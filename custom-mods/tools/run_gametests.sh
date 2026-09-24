#!/usr/bin/env bash
# Headless gametest runner for Hearthwind (all modules).
export JAVA_HOME="${JAVA_HOME:-/usr/local/Cellar/openjdk/26.0.2.1/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"
#
# Boots a throwaway dedicated 26.2 server with fabric-api's gametest
# harness enabled (-Dfabric-api.gametest=true), which runs every @GameTest
# in every mod and writes a JUnit XML report, then parses it.
#
# Usage:  tools/run_gametests.sh [--keep-server] [--filter=<regex>]
#   --filter passes -Dfabric-api.gametest.filter through so a single test
#   (e.g. dungeonz:dungeon_zgame_tests_enter_leave_round_trip_persists_return_point)
#   can be reproduced without running the whole suite.
# Requires: java (25) on PATH. Server files are cached in .gametest-server/
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
CONF="$DIR/../../conversion/build.conf.json"
MC=$(python3 -c "import json;print(json.load(open('$CONF'))['targets']['minecraft'])")
LOADER=$(python3 -c "import json;print(json.load(open('$CONF'))['targets']['loader_version'])")
FABRIC_API=0.158.0+26.2
SRV="$DIR/../.gametest-server"
HEAP="${GAMETEST_HEAP:-1536m}"
KEEP=0
FILTER="${GAMETEST_FILTER:-}"
for arg in "$@"; do
  case "$arg" in
    --keep-server) KEEP=1 ;;
    --filter=*) FILTER="${arg#--filter=}" ;;
  esac
done

cd "$DIR/.."
if [ "${SKIP_BUILD:-0}" = "1" ]; then
  echo "== SKIP_BUILD=1: using pre-staged jars, skipping gradle build =="
else
  echo "== building all hearthwind modules =="
  ./gradlew build --no-daemon --max-workers=2 -q
fi

echo "== running asset and drop integrity tests =="
python3 "$DIR/test_assets_and_drops.py"

echo "== running static validity and attribute linter =="
python3 "$DIR/lint_and_validate.py"

CACHE="$DIR/../.gametest-cache"
mkdir -p "$CACHE" "$SRV"
# Version-stamp the cached launcher: a stale cache from an older loader
# survives across runs (and inside the shared container volume) and would
# boot the wrong loader forever.
if [ ! -f "$CACHE/fabric-server.jar" ] || [ "$(cat "$CACHE/loader.version" 2>/dev/null)" != "$MC/$LOADER" ]; then
  echo "== fetching fabric server launcher ($MC/$LOADER) =="
  curl -sL -o "$CACHE/fabric-server.jar" \
    "https://meta.fabricmc.net/v2/versions/loader/$MC/$LOADER/1.1.0/server/jar"
  echo "$MC/$LOADER" > "$CACHE/loader.version"
fi
cp "$CACHE/fabric-server.jar" "$SRV/fabric-server.jar"

GAMETEST_API=4.0.21+4a7fa0819e
if [ ! -f "$CACHE/fabric-gametest-api-v1.jar" ]; then
  echo "== fetching fabric-gametest-api-v1 =="
  curl -sL -o "$CACHE/fabric-gametest-api-v1.jar" \
    "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-gametest-api-v1/$GAMETEST_API/fabric-gametest-api-v1-$GAMETEST_API.jar"
fi

echo "== installing fresh mod jars =="
rm -rf "$SRV/mods" && mkdir -p "$SRV/mods"
# Pack dist is authoritative for third-party mods (fabric-api et al.).
# dev-server/ is optional and often holds STALE different-filename jars with
# the same fabric.mod.json id (fabric-api 0.159 vs 0.161, strawberrylib r3
# vs r5) - loading both breaks CCA entity_sync on mock players. Copy order:
# dist -> vendored (fill gaps) -> custom builds -> then dedupe by mod id.
cp "$DIR/../../conversion/build/dist/server/mods/"*.jar "$SRV/mods/" 2>/dev/null || true
cp "$DIR/../../conversion/vendored/"*.jar "$SRV/mods/" 2>/dev/null || true
# dev-server only contributes jars whose mod id is not already present
python3 - "$SRV/mods" "$DIR/../../dev-server/mods" <<'PY'
import json, sys, zipfile
from pathlib import Path
dest, dev = Path(sys.argv[1]), Path(sys.argv[2])
if not dev.is_dir():
    sys.exit(0)
have = set()
for j in dest.glob("*.jar"):
    try:
        with zipfile.ZipFile(j) as z:
            have.add(json.loads(z.read("fabric.mod.json")).get("id"))
    except Exception:
        pass
for j in sorted(dev.glob("*.jar")):
    try:
        with zipfile.ZipFile(j) as z:
            mid = json.loads(z.read("fabric.mod.json")).get("id")
    except Exception:
        continue
    if mid and mid not in have:
        (dest / j.name).write_bytes(j.read_bytes())
        have.add(mid)
PY
# Ensure fresh custom builds overwrite any stale jars (letsdo jars included:
# their gametest entrypoints run in the same harness invocation; skip dirs
# not wired in settings.gradle yet)
LETS_DO="letsdo-farm-and-charm letsdo-bakery letsdo-vinery letsdo-brewery letsdo-candlelight letsdo-herbalbrews letsdo-meadow letsdo-nethervinery"
HAVE_LETS_DO=""
for m in $LETS_DO; do [ -d "$m" ] && HAVE_LETS_DO="$HAVE_LETS_DO $m"; done
HAVE_LETS_DO_WIRED=""
for m in $HAVE_LETS_DO; do grep -q "^include '$m'$" settings.gradle && HAVE_LETS_DO_WIRED="$HAVE_LETS_DO_WIRED $m"; done
find hearthwind-survival hearthwind-skills hearthwind-jobs hearthwind-primitive hearthwind-world hearthwind-client athena chipped dungeonz exposure passable-foliage lavender profundis $HAVE_LETS_DO_WIRED -name "*.jar" \
     -path "*build/libs/*" ! -name "*-sources.jar" -exec cp {} "$SRV/mods/" \;
# Install gametest harness
cp "$CACHE/fabric-gametest-api-v1.jar" "$SRV/mods/"
# Final safety: one jar per fabric.mod.json id (prefer pack MC version in
# the file version, then higher version). boids shipped both +26.2 and a
# broken +26.3 mixin-plugin build - lexicographic max would pick 26.3.
python3 - "$SRV/mods" <<'PY'
import json, sys, zipfile
from pathlib import Path
mods = Path(sys.argv[1])
mc = "26.2"
try:
    conf = json.loads((mods.parent.parent / "conversion" / "build.conf.json").read_text())
    # path is custom-mods/.gametest-server -> repo is parents[2]
except Exception:
    pass
# resolve build.conf from harness CWD layout
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
    # prefer versions that contain the pack MC id, then lexicographic max
    return (mc in ver, ver)

by_id = {}
for j in sorted(mods.glob("*.jar")):
    if j.name == "fabric-gametest-api-v1.jar":
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
# report any remaining multi-version ambiguity for boids-like cases
for mid, entries in by_id.items():
    if len(entries) > 1:
        print(f"WARN multi remaining? {mid}: {[e[1].name for e in entries]}")
PY

# Ship the migrated tuning corpus with the throwaway world so gametests read
# the same data the dev server runs (world datapacks override mod resources,
# which is exactly the override order SkillGates/SieveBlock rely on).
mkdir -p "$SRV/world/datapacks"
rm -rf "$SRV/world/datapacks/hearthwind"
cp -R "$DIR/../../conversion/datapacks/hearthwind" "$SRV/world/datapacks/"

grep -q "^eula=true$" "$SRV/eula.txt" 2>/dev/null || echo "eula=true" > "$SRV/eula.txt"
# minimal properties: gametest mode ignores most, but the file must exist
# and empty-pause must not suspend the tick loop mid-run
cat > "$SRV/server.properties" <<'PROPS'
pause-when-empty-seconds=-1
level-type=minecraft\:flat
online-mode=false
view-distance=2
simulation-distance=2
PROPS

REPORT="$SRV/gametest-report.xml"
rm -f "$REPORT"

MERGED_JAR=$(ls "$DIR/../.gradle/loom-cache/minecraftMaven/net/minecraft/"minecraft-merged-*/26.2/minecraft-merged-*-26.2.jar 2>/dev/null | head -1 || true)

echo "== static feature-order cycle check =="
ANALYZER_ARGS=(--mods-dir "$SRV/mods"
  --datapack "$DIR/../../conversion/datapacks/hearthwind"
  --baseline "$DIR/feature_cycle_baseline.txt"
  --strict)
if [ -n "$MERGED_JAR" ]; then ANALYZER_ARGS+=(--vanilla "$MERGED_JAR"); fi
if python3 "$DIR/check_feature_cycles.py" "${ANALYZER_ARGS[@]}"; then
  echo "static cycle check: OK"
else
  echo "FAIL: NEW feature-order cycle(s) detected (update datapack fixes or baseline)"
  exit 2
fi

echo "== running gametests headless (${HEAP} heap) =="
FILTER_ARGS=()
if [ -n "$FILTER" ]; then
  echo "== gametest filter: $FILTER =="
  FILTER_ARGS+=("-Dfabric-api.gametest.filter=$FILTER")
fi
set +e
cd "$SRV"
timeout "${GAMETEST_TIMEOUT:-1200}" java -Xmx"$HEAP" \
     -Dfabric-api.gametest=true \
     -Dhearthwind.mergedJar="${MERGED_JAR}" \
     "${FILTER_ARGS[@]:+${FILTER_ARGS[@]}}" \
     -Dfabric-api.gametest.report-file="$REPORT" \
     -jar "$SRV/fabric-server.jar" nogui > "$SRV/gametest.log" 2>&1
STATUS=$?
set -e

echo "== worldgen cycle smoke check =="
CYCLE_RC=0
if grep -q "Feature order cycle" "$SRV/gametest.log"; then
  echo "FAIL: 'Feature order cycle' in server log (cycle-tolerant mixin not applied?)"
  CYCLE_RC=2
fi
if grep -q "Error upgrading chunk" "$SRV/gametest.log"; then
  echo "FAIL: chunk feature upgrade errors in server log"
  CYCLE_RC=2
fi
DROPS=$(grep -c "Dropped feature-order back-edge" "$SRV/gametest.log" || true)
echo "tolerant back-edge drops in this boot: ${DROPS}"

echo "== server log tail =="
tail -5 "$SRV/gametest.log"

if [ ! -f "$REPORT" ]; then
  echo "FAIL: no gametest report produced (see $SRV/gametest.log)"
  exit 2
fi

python3 "$DIR/parse_gametest_report.py" "$REPORT"
RC=$?
if [ $CYCLE_RC -ne 0 ]; then
  RC=$CYCLE_RC
fi

if [ $RC -eq 0 ] && [ $STATUS -ne 0 ]; then
  echo "note: tests passed but server exited with status $STATUS"
fi

if [ $KEEP -eq 0 ]; then
  rm -rf "$SRV"
fi
exit $RC
