#!/usr/bin/env bash
# Setup local PrismLauncher dev instances for Hearthwind (client + server)
# Uses current Prism install at ~/Library/Application Support/PrismLauncher
# and current built jars in conversion/build/dist + custom-mods/*/build/libs
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
PRISM_INST="$HOME/Library/Application Support/PrismLauncher/instances"
SRC_INST="$PRISM_INST/26.2"
DST_INST="$PRISM_INST/Hearthwind-Dev-Client"

if [ ! -d "$SRC_INST" ]; then
  echo "Source vanilla instance $SRC_INST not found — create a vanilla 26.2 instance in Prism first"
  exit 1
fi

echo "== Prism client: Hearthwind-Dev-Client =="
rm -rf "$DST_INST"
cp -R "$SRC_INST" "$DST_INST"

python3 - << 'PY'
import json, pathlib
p = pathlib.Path.home() / "Library/Application Support/PrismLauncher/instances/Hearthwind-Dev-Client/mmc-pack.json"
data = json.loads(p.read_text())
if not any(c.get("uid")=="net.fabricmc.fabric-loader" for c in data["components"]):
    data["components"].append({"uid":"net.fabricmc.fabric-loader","version":"0.19.3","cachedName":"Fabric Loader","cachedVersion":"0.19.3"})
p.write_text(json.dumps(data, indent=4))
print("patched mmc-pack.json with fabric-loader 0.19.3")
PY

python3 - << 'PY'
import pathlib, re
p = pathlib.Path.home() / "Library/Application Support/PrismLauncher/instances/Hearthwind-Dev-Client/instance.cfg"
t = p.read_text()
t = t.replace("name=26.2","name=Hearthwind-Dev-Client")
import re as re2
if "MaxMemAlloc" in t:
    t = re2.sub(r"MaxMemAlloc=\d+","MaxMemAlloc=4096",t)
p.write_text(t)
print("patched instance.cfg")
PY

MODS_SRC="$ROOT/conversion/build/dist/client/mods"
if [ ! -d "$MODS_SRC" ]; then MODS_SRC="$ROOT/conversion/build/dist/server/mods"; fi
DST_MODS="$DST_INST/minecraft/mods"
mkdir -p "$DST_MODS"
rm -f "$DST_MODS"/hearthwind-*.jar
echo "Copying mods from $MODS_SRC"
cp "$MODS_SRC"/*.jar "$DST_MODS"/ 2>/dev/null || true
for f in "$ROOT"/custom-mods/hearthwind-*/build/libs/*26.2+0.1.0.jar; do
  [[ -f "$f" ]] || continue
  [[ "$f" == *"-sources.jar" ]] && continue
  cp "$f" "$DST_MODS"/
done
echo "Client mods: $(ls "$DST_MODS"/*.jar 2>/dev/null | wc -l) jars"
for f in "$DST_MODS"/hearthwind-*.jar; do
  [[ -f "$f" ]] || continue
  src="$ROOT/custom-mods/$(basename "${f%%/build/libs/*}")/build/libs/$(basename "$f")"
  cmp -s "$src" "$f" || { echo "ERROR: $f does not match $src" >&2; exit 1; }
done
echo "verified hearthwind client jars match build output"

# Dev server
SRC_SRV="$ROOT/conversion/build/dist/server"
DST_SRV="$ROOT/dev-server"
echo ""
echo "== Dev server: $DST_SRV =="
mkdir -p "$DST_SRV"
if [ -d "$SRC_SRV/mods" ]; then
  rm -rf "$DST_SRV/mods"
  mkdir -p "$DST_SRV/world/datapacks"
  cp -R "$SRC_SRV/mods" "$DST_SRV/"
  cp -R "$SRC_SRV/world/datapacks/hearthwind" "$DST_SRV/world/datapacks/" 2>/dev/null || true
else
  echo "No server dist — run python3 conversion/scripts/resolve_deps.py && python3 conversion/scripts/build_pack.py --server-dir && ./gradlew build"
  exit 1
fi
for f in "$ROOT"/custom-mods/hearthwind-*/build/libs/*26.2+0.1.0.jar; do
  [[ -f "$f" ]] || continue
  [[ "$f" == *"-sources.jar" ]] && continue
  [[ "$f" == *"hearthwind-client"* ]] && continue
  cp "$f" "$DST_SRV/mods/"
done
if [ ! -f "$DST_SRV/fabric-server.jar" ]; then
  curl -sL -o "$DST_SRV/fabric-server.jar" "https://meta.fabricmc.net/v2/versions/loader/26.2/0.19.3/1.1.0/server/jar"
fi
echo "eula=true" > "$DST_SRV/eula.txt"
cat > "$DST_SRV/server.properties" <<'PROPS'
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
echo "Dev server mods: $(ls "$DST_SRV/mods"/*.jar 2>/dev/null | wc -l) jars"
echo ""
echo "Done."
echo "  Client: launch Hearthwind-Dev-Client in PrismLauncher (will download Fabric 0.19.3 on first launch)"
echo "  Server: cd dev-server && java -Xmx3G -jar fabric-server.jar nogui"
echo "  Connect client to localhost:25565 ; RCON: python3 custom-mods/tools/rcon.py 127.0.0.1 25575 agedtest list"
