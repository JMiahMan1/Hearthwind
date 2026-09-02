#!/usr/bin/env bash
# Headless gametest runner for Hearthwind (all modules).
export JAVA_HOME="${JAVA_HOME:-/usr/local/Cellar/openjdk/26.0.2.1/libexec/openjdk.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"
#
# Boots a throwaway dedicated 26.2 server with fabric-api's gametest
# harness enabled (-Dfabric-api.gametest=true), which runs every @GameTest
# in every mod and writes a JUnit XML report, then parses it.
#
# Usage:  tools/run_gametests.sh [--keep-server]
# Requires: java (25) on PATH. Server files are cached in .gametest-server/
set -euo pipefail

MC=26.2
LOADER=0.19.3
FABRIC_API=0.158.0+26.2
DIR="$(cd "$(dirname "$0")" && pwd)"
SRV="$DIR/../.gametest-server"
HEAP="${GAMETEST_HEAP:-768m}"
KEEP=0
[ "${1:-}" = "--keep-server" ] && KEEP=1

cd "$DIR/.."
echo "== building all hearthwind modules =="
./gradlew build --no-daemon --max-workers=2 -q

echo "== running asset and drop integrity tests =="
python3 "$DIR/test_assets_and_drops.py"

echo "== running static validity and attribute linter =="
python3 "$DIR/lint_and_validate.py"

CACHE="$DIR/../.gametest-cache"
mkdir -p "$CACHE" "$SRV"
if [ ! -f "$CACHE/fabric-server.jar" ]; then
  echo "== fetching fabric server launcher =="
  curl -sL -o "$CACHE/fabric-server.jar" \
    "https://meta.fabricmc.net/v2/versions/loader/$MC/$LOADER/1.1.0/server/jar"
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
# Copy all resolved server dependencies and vendored jars
cp "$DIR/../../dev-server/mods/"*.jar "$SRV/mods/" 2>/dev/null || true
cp "$DIR/../../conversion/vendored/"*.jar "$SRV/mods/" 2>/dev/null || true
# Ensure fresh custom builds overwrite any stale jars
find hearthwind-survival hearthwind-skills hearthwind-jobs hearthwind-primitive hearthwind-world hearthwind-client -name "*.jar" \
     -path "*build/libs/*" ! -name "*-sources.jar" -exec cp {} "$SRV/mods/" \;
# Install gametest harness
cp "$CACHE/fabric-gametest-api-v1.jar" "$SRV/mods/"

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
set +e
cd "$SRV"
timeout "${GAMETEST_TIMEOUT:-420}" java -Xmx"$HEAP" \
     -Dfabric-api.gametest=true \
     -Dhearthwind.mergedJar="${MERGED_JAR}" \
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
