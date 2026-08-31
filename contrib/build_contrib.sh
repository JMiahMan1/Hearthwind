#!/usr/bin/env bash
# Master build script for all contrib/third-party ported mods for Hearthwind 26.2.
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== [1/2] Building Contrib: Nature's Spirit 26.2 ==="
REPO="$(cd "$DIR/.." && pwd)"
TMP="$REPO/.tmp"
mkdir -p "$TMP" "$REPO/conversion/vendored"

NS_DIR="$TMP/NaturesSpirit"
if [ ! -d "$NS_DIR" ]; then
    echo "Cloning Team-Hibiscus/NaturesSpirit (branch 26.1.2)..."
    git clone --branch 26.1.2 --depth 1 https://github.com/Team-Hibiscus/NaturesSpirit.git "$NS_DIR"
    echo "Applying 26.2 port patch..."
    git -C "$NS_DIR" apply "$REPO/contrib/natures-spirit/patches/26.2-port.patch"
fi

echo "Compiling Nature's Spirit Fabric jar..."
(cd "$NS_DIR" && ./gradlew :fabric:build --no-daemon)
cp "$NS_DIR/fabric/build/libs/natures_spirit-fabric-2.3.0+26.2.jar" "$REPO/conversion/vendored/"
echo "Nature's Spirit 26.2 jar ready in conversion/vendored/"

echo "=== [2/6] Building Contrib: YUNG's Suite 26.2 ==="
bash "$DIR/build_yungs.sh"

echo "=== [3/6] Building Contrib: Gardens of the Dead 26.2 ==="
bash "$DIR/build_gardens_of_the_dead.sh"

echo "=== [4/6] Building Contrib: The Lost Castle 26.2 ==="
bash "$DIR/the-lost-castle/build_the_lost_castle.sh"

echo "=== [5/6] Building Contrib: Birds Boids 26.2 ==="
bash "$DIR/birds-boids/build_birds_boids.sh"

echo "=== [6/6] Building Contrib: Medieval Buildings & True Ending 26.2 ==="
bash "$DIR/medieval-buildings/build_medieval_buildings.sh"
bash "$DIR/true-ending/build_true_ending.sh"

echo "=== All Contrib Builds Completed Successfully ==="
