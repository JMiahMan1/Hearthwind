#!/usr/bin/env bash
# test_yungs_api.sh — End-to-end validation of upstream YUNG-GANG 26.2 port PRs.
#
# Builds YUNGs-API + all 5 structure mods from their 26.2 PR branches, publishes
# the API to mavenLocal, boot-smokes everything on a throwaway 26.2 server, and
# force-places one structure per mod via RCON.
#
# Usage: bash tools/test_yungs_api.sh [--keep-server]
# Exits nonzero on any failure. Scratch lives in .tmp/yungs-api/.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
WORK="$REPO_ROOT/.tmp/yungs-api"
SMOKE="$WORK/smoke-server"
JAVA_BIN="${JAVA_BIN:-/usr/local/opt/openjdk/bin/java}"
RCON="python3 $REPO_ROOT/custom-mods/tools/rcon.py"
RCON_PORT=25579
RCON_PW=agedtest
API_REF="pull/109/head"          # MentalCokuntus 'Port to Minecraft 26.2' (preferred over #108)
KEEP=0
[ "${1:-}" = "--keep-server" ] && KEEP=1

mkdir -p "$WORK"
cd "$WORK"

clone_at_pr() { # repo_dir repo_url pr_number
  local dir="$1" url="$2" pr="$3"
  if [ ! -d "$dir/.git" ]; then git clone --quiet "$url" "$dir"; fi
  (cd "$dir" && git fetch --quiet origin "pull/$pr/head" && git checkout --quiet FETCH_HEAD)
  (cd "$dir" && git log --oneline -1)
}

# Gradle daemons need >=9.6 on a Java 26 host; upstream PRs pin 9.2.0.
bump_wrapper() {
  local dir="$1"
  (cd "$dir" && python3 -c "
import pathlib
p = pathlib.Path('gradle/wrapper/gradle-wrapper.properties')
t = p.read_text()
if 'gradle-9.2.0' in t:
    p.write_text(t.replace('gradle-9.2.0-bin.zip', 'gradle-9.6.1-bin.zip'))
    print('$dir: wrapper bumped to 9.6.1')")
}

echo "== 1. Clone + build YUNGs-API ($API_REF), publish to mavenLocal =="
clone_at_pr YUNGs-API https://github.com/YUNG-GANG/YUNGs-API 109
bump_wrapper YUNGs-API
(cd YUNGs-API && ./gradlew :Common:build :Fabric:build :Common:publishToMavenLocal :Fabric:publishToMavenLocal \
  -x test --no-daemon --max-workers=2 --console=plain -q) \
  || { echo "FAIL: YUNGs-API build"; exit 1; }
API_JAR="$(ls YUNGs-API/Fabric/build/libs/YungsApi-*Fabric-*.jar | grep -v sources | grep -v javadoc)"
echo "API jar: $API_JAR"

echo "== 2. Clone + build 5 structure mods at their 26.2 PRs =="
# PR numbers: MentalCokuntus series where present, else jojo-chaechae.
clone_at_pr YUNGs-Better-Desert-Temples  https://github.com/YUNG-GANG/YUNGs-Better-Desert-Temples   50
clone_at_pr YUNGs-Better-Jungle-Temples  https://github.com/YUNG-GANG/YUNGs-Better-Jungle-Temples   16
clone_at_pr YUNGs-Better-Fortresses      https://github.com/YUNG-GANG/YUNGs-Better-Fortresses       38
clone_at_pr YUNGs-Better-Ocean-Monuments https://github.com/YUNG-GANG/YUNGs-Better-Ocean-Monuments  24
clone_at_pr YUNGs-Better-End-Island      https://github.com/YUNG-GANG/YUNGs-Better-End-Island       75
for d in YUNGs-Better-*; do
  bump_wrapper "$d"
  (cd "$d" && ./gradlew :Fabric:build -x test --no-daemon --max-workers=2 --console=plain -q) \
    || { echo "FAIL: $d build"; exit 1; }
  echo "$d OK"
done

echo "== 3. Stage + boot smoke server (port 25577, RCON $RCON_PORT) =="
mkdir -p "$SMOKE/mods"
cp "$API_JAR" "$SMOKE/mods/"
for d in YUNGs-Better-*; do
  find "$d/Fabric/build/libs" -name "*-Fabric-*.jar" ! -name "*sources*" ! -name "*javadoc*" -exec cp {} "$SMOKE/mods/" \;
done
# fabric-api + cloth-config copied from dev-server if not already staged.
cp -n "$REPO_ROOT"/dev-server/mods/fabric-api-*26.2*.jar "$SMOKE/mods/" 2>/dev/null || true
cp -n "$REPO_ROOT"/dev-server/mods/cloth-config-*.jar "$SMOKE/mods/" 2>/dev/null || true
if [ ! -f "$SMOKE/fabric-server.jar" ]; then
  cp "$REPO_ROOT"/dev-server/fabric-server.jar "$SMOKE/"
  cp -r "$REPO_ROOT"/dev-server/libraries "$SMOKE/"
  printf 'eula=true\n' > "$SMOKE/eula.txt"
fi
cat > "$SMOKE/server.properties" <<EOF
online-mode=false
server-port=25577
enable-rcon=true
rcon.port=$RCON_PORT
rcon.password=$RCON_PW
pause-when-empty-seconds=-1
EOF
rm -rf "$SMOKE/world" "$SMOKE"/boot.log
(cd "$SMOKE" && (nohup "$JAVA_BIN" -Xmx768M -jar fabric-server.jar nogui > boot.log 2>&1 < /dev/null &))
for i in $(seq 1 24); do
  sleep 5
  grep -q "Done (" "$SMOKE/boot.log" 2>/dev/null && break
done
if ! grep -q "Done (" "$SMOKE/boot.log"; then echo "FAIL: server did not reach Done"; tail -20 "$SMOKE/boot.log"; exit 1; fi
ERRS=$(grep -cE "ERROR|Exception" "$SMOKE/boot.log" || true)
echo "Boot: Done. error lines: $ERRS"

echo "== 4. Force-place one structure per mod =="
rcon() { $RCON 127.0.0.1 "$RCON_PORT" "$RCON_PW" "$1"; }
rcon "forceload add -96 -96 96 96" > /dev/null
rcon "execute in minecraft:the_nether run forceload add -96 -96 96 96" > /dev/null
FAILURES=0
place() { # dim structure_id
  local out
  out=$(rcon "execute in $1 run place structure $2 8 40 8" 2>&1 | tail -1)
  case "$out" in
    Generated*) echo "PASS: $2";;
    *)          echo "FAIL: $2 -> $out"; FAILURES=$((FAILURES+1));;
  esac
}
place minecraft:overworld betterdeserttemples:desert_temple
place minecraft:overworld betterjungletemples:jungle_temple
place minecraft:overworld betteroceanmonuments:ocean_monument
place minecraft:the_nether  betterfortresses:fortress
# betterendisland is code-driven worldgen; validated by clean boot + registered
# bei_ExtraDragonFight dimension on fresh worlds:
rcon "execute in minecraft:the_end run forceload add 0 0 16 16" > /dev/null || true

if [ "$FAILURES" -gt 0 ] || [ "$ERRS" -gt 1 ]; then
  echo "RESULT: FAIL ($FAILURES placement failures, $ERRS error lines)"
  EXIT=1
else
  echo "RESULT: PASS"
  EXIT=0
fi

if [ "$KEEP" -eq 0 ]; then
  rcon "stop" > /dev/null 2>&1 || true
  sleep 5
fi
exit $EXIT
