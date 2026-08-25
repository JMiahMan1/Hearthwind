#!/usr/bin/env bash
# Headless gametest runner for Hearthwind (all modules).
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

mkdir -p "$SRV/mods"
if [ ! -f "$SRV/fabric-server.jar" ]; then
  echo "== fetching fabric server launcher =="
  curl -sL -o "$SRV/fabric-server.jar" \
    "https://meta.fabricmc.net/v2/versions/loader/$MC/$LOADER/1.1.0/server/jar"
fi
GAMETEST_API=4.0.21+4a7fa0819e
if [ ! -f "$SRV/mods/fabric-api.jar" ]; then
  echo "== fetching fabric-api =="
  curl -sL -o "$SRV/mods/fabric-api.jar" \
    "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$FABRIC_API/fabric-api-$FABRIC_API.jar"
fi
# the maven fabric-api jar is THIN (no nested modules): ship the gametest
# module explicitly, otherwise -Dfabric-api.gametest never activates
if [ ! -f "$SRV/mods/fabric-gametest-api-v1.jar" ]; then
  echo "== fetching fabric-gametest-api-v1 =="
  curl -sL -o "$SRV/mods/fabric-gametest-api-v1.jar" \
    "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-gametest-api-v1/$GAMETEST_API/fabric-gametest-api-v1-$GAMETEST_API.jar"
fi

echo "== installing fresh mod jars =="
rm -f "$SRV"/mods/hearthwind-*.jar
# every custom module ships its plain jar so cross-module behavior is
# exercised together (never the -sources jars). hearthwind-client is
# client-only ("environment": "client") - the dedicated server ignores it,
# so we exclude it here; it is exercised by client-gametest runs instead.
find hearthwind-survival hearthwind-skills hearthwind-jobs hearthwind-primitive hearthwind-world -name "*.jar" \
     -path "*build/libs/*" ! -name "*-sources.jar" -exec cp {} "$SRV/mods/" \;
ls "$SRV"/mods/

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

echo "== running gametests headless (${HEAP} heap) =="
set +e
cd "$SRV"
timeout "${GAMETEST_TIMEOUT:-420}" java -Xmx"$HEAP" \
     -Dfabric-api.gametest=true \
     -Dfabric-api.gametest.report-file="$REPORT" \
     -jar "$SRV/fabric-server.jar" nogui > "$SRV/gametest.log" 2>&1
STATUS=$?
set -e

echo "== server log tail =="
tail -5 "$SRV/gametest.log"

if [ ! -f "$REPORT" ]; then
  echo "FAIL: no gametest report produced (see $SRV/gametest.log)"
  exit 2
fi

python3 "$DIR/parse_gametest_report.py" "$REPORT"
RC=$?

if [ $RC -eq 0 ] && [ $STATUS -ne 0 ]; then
  echo "note: tests passed but server exited with status $STATUS"
fi

if [ $KEEP -eq 0 ]; then
  rm -rf "$SRV"
fi
exit $RC
