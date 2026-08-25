#!/usr/bin/env bash
# Install Hearthwind as systemd service on a Linux host (Ubuntu/Debian)
# Usage: sudo bash deploy/install.sh [/opt/hearthwind]
set -euo pipefail
DST="${1:-/opt/hearthwind}"
USER=hearthwind
SERVICE_SRC="$(cd "$(dirname "$0")/systemd" && pwd)/hearthwind.service"

if [ "$EUID" -ne 0 ]; then echo "Run as root: sudo bash deploy/install.sh"; exit 1; fi

echo "== Creating user $USER =="
id -u $USER >/dev/null 2>&1 || useradd -r -m -d "$DST" -s /bin/bash "$USER"

echo "== Installing to $DST =="
mkdir -p "$DST"
# Prefer plain server zip from Releases: Hearthwind-*.mrpack + hearthwind-server zip
# If running from repo, copy current built server:
if [ -d "conversion/build/dist/server/mods" ]; then
  echo "Copying conversion/build/dist/server (local build)..."
  cp -R conversion/build/dist/server/mods "$DST"/
  cp -R conversion/build/dist/server/world "$DST"/ 2>/dev/null || mkdir -p "$DST/world"
  for f in custom-mods/hearthwind-*/build/libs/*26.2+0.1.0.jar; do
    [[ -f "$f" ]] || continue
    [[ "$f" == *"-sources.jar" ]] && continue
    [[ "$f" == *"hearthwind-client"* ]] && continue
    cp "$f" "$DST/mods/"
  done
  if [ ! -f "$DST/fabric-server.jar" ]; then
    curl -sL -o "$DST/fabric-server.jar" "https://meta.fabricmc.net/v2/versions/loader/26.2/0.19.3/1.1.0/server/jar"
  fi
else
  echo "No local build found - download Hearthwind-*.mrpack or hearthwind-server zip from Releases and unpack to $DST"
  echo "See docs/INSTALL.md"
fi

echo "eula=true" > "$DST/eula.txt"
if [ ! -f "$DST/server.properties" ]; then
cat > "$DST/server.properties" <<'PROPS'
pause-when-empty-seconds=-1
enable-rcon=true
rcon.port=25575
rcon.password=CHANGE_ME_agedtest
view-distance=8
simulation-distance=6
level-type=minecraft:normal
online-mode=false
server-port=25565
PROPS
  echo "Created $DST/server.properties - EDIT rcon.password!"
fi

chown -R $USER:$USER "$DST"
chmod +x "$DST/fabric-server.jar" 2>/dev/null || true

echo "== Installing systemd unit =="
cp "$SERVICE_SRC" /etc/systemd/system/hearthwind.service
# Fix WorkingDirectory/User if DST != /opt/hearthwind
if [ "$DST" != "/opt/hearthwind" ]; then
  sed -i "s|WorkingDirectory=/opt/hearthwind|WorkingDirectory=$DST|;s|User=hearthwind|User=$USER|" /etc/systemd/system/hearthwind.service
fi
systemctl daemon-reload
systemctl enable hearthwind
echo ""
echo "== Done =="
echo "  sudo systemctl start hearthwind"
echo "  sudo journalctl -u hearthwind -f        # logs"
echo "  sudo systemctl status hearthwind"
echo "  RCON: python3 custom-mods/tools/rcon.py 127.0.0.1 25575 CHANGE_ME_agedtest list"
echo "  Gamemode survivors join vanilla 26.2 at <host>:25565"
