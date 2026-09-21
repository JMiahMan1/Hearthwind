#!/usr/bin/env bash
# Stages everything containerized test runs need into a Docker-mountable dir.
#
# WHY: Docker Desktop on macOS cannot bind-mount paths under /Users/<name>
# (sticky-bit home dir -> "mkdir ...: file exists"), but /tmp mounts fine.
# This script mirrors the minimal repo subset + host-built jars + the few
# gradle-cache jars the harnesses resolve at runtime into
# /tmp/cgthearthwind-stage (persistent: provision + server-jar caches live
# there and are never wiped). Per project policy all *test execution* runs
# in containers; hosts only build (`./gradlew build` first) and stage.
#
# Layout inside the stage:
#   repo/...          mirror of the repo subset (mounted at /work/repo)
#   gradle-home/...   selective gradle cache (mounted at /root/.gradle)
#
# Usage: bash tools/stage_container_tests.sh
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(cd "$DIR/../.." && pwd)"
STAGE="${CGT_STAGE_DIR:-/tmp/cgthearthwind-stage}"
R="$STAGE/repo"
G="$STAGE/gradle-home/caches/modules-2/files-2.1"

echo "== staging container test inputs -> $STAGE"
mkdir -p "$R" "$G"

# 1. tooling + build wiring (source of truth: repo)
rsync -a --delete "$REPO/custom-mods/tools/" "$R/custom-mods/tools/"
cp "$REPO/custom-mods/settings.gradle" "$R/custom-mods/settings.gradle"
# loom merged jar (mixin-shadow tests read client classes from it via
# -Dhearthwind.mergedJar, resolved by run_gametests.sh under .gradle/)
mkdir -p "$R/custom-mods/.gradle/loom-cache"
rsync -a --delete "$REPO/custom-mods/.gradle/loom-cache/" "$R/custom-mods/.gradle/loom-cache/"

# 2. fresh host-built jars (plain only, never -sources)
for d in "$REPO"/custom-mods/hearthwind-*/build/libs \
         "$REPO"/custom-mods/athena/build/libs \
         "$REPO"/custom-mods/chipped/build/libs \
         "$REPO"/custom-mods/dungeonz/build/libs \
          "$REPO"/custom-mods/exposure/build/libs \
          "$REPO"/custom-mods/passable-foliage/build/libs \
         "$REPO"/custom-mods/smallships/build/libs \
         "$REPO"/custom-mods/villagesandpillages/build/libs \
         "$REPO"/custom-mods/letsdo-*/build/libs; do
  [ -d "$d" ] || continue
  mod="$(basename "$(dirname "$(dirname "$d")")")"
  dest="$R/custom-mods/$mod/build/libs"
  mkdir -p "$dest"
  for j in "$d"/*26.2+0.1.0.jar; do
    [ -f "$j" ] || continue
    case "$j" in *sources*) continue ;; esac
    cp "$j" "$dest/"
  done
done

# 3. pack mod sets + tuning corpus + server base mods
# build.conf.json is the version source of truth run_gametests.sh reads.
mkdir -p "$R/conversion"
cp "$REPO/conversion/build.conf.json" "$R/conversion/build.conf.json"
rsync -a --delete "$REPO/conversion/build/dist/server/mods/" "$R/conversion/build/dist/server/mods/"
rsync -a --delete "$REPO/conversion/build/dist/client/mods/" "$R/conversion/build/dist/client/mods/"
rsync -a --delete "$REPO/conversion/datapacks/hearthwind/" "$R/conversion/datapacks/hearthwind/"
rsync -a --delete "$REPO/dev-server/mods/" "$R/dev-server/mods/"
[ -d "$REPO/conversion/vendored" ] && rsync -a --delete "$REPO/conversion/vendored/" "$R/conversion/vendored/" || true

# 4. gradle-cache jars the harnesses resolve at runtime (keep relative paths
#    so the existing glob lookups match)
CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/caches/modules-2/files-2.1"
need=(
  "net.fabricmc/fabric-loader/*/*/fabric-loader-*.jar"
  "net.fabricmc/sponge-mixin/*/*/sponge-mixin-*.jar"
  "io.github.llamalad7/mixinextras-fabric/*/*/mixinextras-fabric-*.jar"
  "org.ow2.asm/asm/9*/*/asm-9*.jar"
  "org.ow2.asm/asm-tree/9*/*/asm-tree-9*.jar"
  "org.ow2.asm/asm-commons/9*/*/asm-commons-9*.jar"
  "org.ow2.asm/asm-util/9*/*/asm-util-9*.jar"
  "org.ow2.asm/asm-analysis/9*/*/asm-analysis-9*.jar"
  "net.fabricmc.fabric-api/fabric-client-gametest-api-v1/*/*/*.jar"
)
shopt -s nullglob
for pat in "${need[@]}"; do
  for j in "$CACHE"/$pat; do
    case "$j" in *sources*) continue ;; esac
    rel="${j#$CACHE/}"
    mkdir -p "$G/$(dirname "$rel")"
    cp "$j" "$G/$rel"
  done
done
shopt -u nullglob

echo "stage: $(du -sh "$STAGE" | cut -f1) at $STAGE"
echo "staged jars: $(find "$R" "$G" -name '*.jar' | wc -l | tr -d ' ')"
