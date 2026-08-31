#!/usr/bin/env bash
# Complete live testing suite: drives a live running client to verify HUD, menus, and gameplay mechanics.
set -euo pipefail

REPO="$(cd "$(dirname "$0")/../.." && pwd)"
TOOLS="$REPO/custom-mods/tools"
SHOTS="$REPO/.tmp/shots"

mkdir -p "$SHOTS"

echo "=== 1. Checking dev-server connection ==="
python3 "$TOOLS/rcon.py" 127.0.0.1 25575 agedtest "list"

echo "=== 2. Setting player test state ==="
python3 "$TOOLS/rcon.py" 127.0.0.1 25575 agedtest "effect give TestPlayer minecraft:resistance 100000 3 true"
python3 "$TOOLS/rcon.py" 127.0.0.1 25575 agedtest "execute as TestPlayer run hearthwind hydration set 18"

echo "=== 3. Testing In-Game HUD ==="
python3 "$TOOLS/client_harness.py" activate
python3 "$TOOLS/client_harness.py" shot "live_test_1_hud"
echo "Captured live HUD: $SHOTS/live_test_1_hud.png"

echo "=== 4. Testing Tab 1: Inventory [E] ==="
python3 "$TOOLS/client_harness.py" type "e"
sleep 0.8
python3 "$TOOLS/client_harness.py" shot "live_test_2_inventory"

echo "=== 5. Testing Tab 2: Skills [K] ==="
python3 "$TOOLS/client_harness.py" type "k"
sleep 0.8
python3 "$TOOLS/client_harness.py" shot "live_test_3_skills"

echo "=== 6. Testing Tab 3: Jobs [J] ==="
python3 "$TOOLS/client_harness.py" type "j"
sleep 0.8
python3 "$TOOLS/client_harness.py" shot "live_test_4_jobs"

echo "=== 7. Testing Tab 4: Nutrients [N] ==="
python3 "$TOOLS/client_harness.py" type "n"
sleep 0.8
python3 "$TOOLS/client_harness.py" shot "live_test_5_nutrients"

echo "=== 8. Closing screens ==="
python3 "$TOOLS/client_harness.py" type "e"
sleep 0.5

echo "=========================================="
echo "All live tests executed successfully! Screenshots saved in .tmp/shots/"
