#!/usr/bin/env bash
# Refresh Prism Launcher test instances with freshly built mod jars.
#
# Usage: bash tools/update_prism.sh [--deploy-new <InstanceName>]
#
# Default: for every module in this workspace that has a built PLAIN jar
# (build/libs/<mod>-*.jar, never *-sources.jar), replace the same-mod jar
# in each Hearthwind Prism instance that already carries it.
# --deploy-new <Instance>: additionally copy newly built modules that the
#   named instance does not have yet (e.g. a port that builds for the
#   first time). Default target if no instance given: Hearthwind-Dev-Client.
#
# Instances live at ~/Library/Application Support/PrismLauncher/instances.
# This script only swaps files under <instance>/minecraft/mods/.

set -u
cd "$(dirname "$0")/.." || exit 1

INST_ROOT="$HOME/Library/Application Support/PrismLauncher/instances"
INSTANCES="Hearthwind-Full Hearthwind-Minimal Hearthwind-Dev-Client"
DEPLOY_NEW=""
if [ "${1:-}" = "--deploy-new" ]; then
  DEPLOY_NEW="${2:-Hearthwind-Dev-Client}"
fi

[ -d "$INST_ROOT" ] || { echo "no Prism installs at $INST_ROOT"; exit 1; }

replaced=0; skipped=0; added=0
for moddir in hearthwind-* letsdo-* smallships villagesandpillages athena chipped dungeonz; do
  [ -d "$moddir" ] || continue
  # plain jar only: newest non-sources jar in build/libs
  jar=$(ls -t "$moddir"/build/libs/*.jar 2>/dev/null | grep -v -- "-sources\.jar$" | head -1)
  if [ -z "$jar" ]; then
    echo "SKIP $moddir (not built)"
    skipped=$((skipped + 1))
    continue
  fi
  mod=$(basename "$moddir")
  for inst in $INSTANCES; do
    mdir="$INST_ROOT/$inst/minecraft/mods"
    [ -d "$mdir" ] || { echo "SKIP $inst (no mods dir)"; continue; }
    old=$(ls "$mdir/$mod"-*.jar 2>/dev/null | head -1)
    if [ -n "$old" ]; then
      if cmp -s "$jar" "$old"; then
        echo "SAME $inst/$mod ($(basename "$old"))"
      else
        rm -f "$mdir/$mod"-*.jar
        cp "$jar" "$mdir/"
        echo "UPDATE $inst/$mod: $(basename "$old") -> $(basename "$jar")"
        replaced=$((replaced + 1))
      fi
    elif [ "$inst" = "$DEPLOY_NEW" ]; then
      cp "$jar" "$mdir/"
      echo "ADD $inst/$mod: $(basename "$jar")"
      added=$((added + 1))
    fi
  done
done
echo "prism: $replaced updated, $added added, $skipped unbuilt"
