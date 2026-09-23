#!/usr/bin/env bash
# Refresh Prism Launcher test instances with freshly built mod jars,
# the pinned Fabric loader / fabric-api, and world datapacks.
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
# Also (every run):
#   - bump mmc-pack.json Fabric Loader to conversion/build.conf.json loader_version
#   - replace fabric-api-*.jar with the exact resolved fabric-api file
#   - sync conversion/datapacks/* into minecraft/world/datapacks and every
#     saves/*/datapacks (singleplayer worlds pick packs up on next open)
#   - synthesize mmc-pack.json for instances that lack one
#
# Instances live at ~/Library/Application Support/PrismLauncher/instances.
# This script only touches files under <instance>/minecraft/ and mmc-pack.json.

set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
# tools/ -> custom-mods/ -> repo root
ROOT="$(cd "$DIR/../.." && pwd)"
cd "$ROOT" || exit 1

INST_ROOT="$HOME/Library/Application Support/PrismLauncher/instances"
INSTANCES="Hearthwind-Full Hearthwind-Minimal Hearthwind-Dev-Client"
DEPLOY_NEW=""
if [ "${1:-}" = "--deploy-new" ]; then
  DEPLOY_NEW="${2:-Hearthwind-Dev-Client}"
fi

[ -d "$INST_ROOT" ] || { echo "no Prism installs at $INST_ROOT"; exit 1; }

LOADER_VER=$(python3 -c "import json; print(json.load(open('$ROOT/conversion/build.conf.json'))['targets']['loader_version'])")
[ -n "$LOADER_VER" ] || { echo "ERROR: empty loader_version from build.conf.json"; exit 1; }
API_JAR=$(ls -1 "$ROOT"/conversion/build/dist/server/mods/fabric-api-*.jar 2>/dev/null | grep -v -- "-sources" | head -1)
if [ -z "$API_JAR" ]; then
  echo "WARN: no fabric-api jar under conversion/build/dist/server/mods (run build_pack.py --server-dir)"
fi
API_BASE=$(basename "${API_JAR:-fabric-api-}")

echo "loader=$LOADER_VER api=${API_BASE}"

# --- loader bump + mmc-pack synthesis + fabric-api swap + datapack sync ---
for inst in $INSTANCES; do
  idir="$INST_ROOT/$inst"
  [ -d "$idir" ] || { echo "SKIP $inst (missing)"; continue; }

  # Synthesize mmc-pack.json from Hearthwind-Full when absent
  if [ ! -f "$idir/mmc-pack.json" ]; then
    src="$INST_ROOT/Hearthwind-Full/mmc-pack.json"
    if [ -f "$src" ]; then
      cp "$src" "$idir/mmc-pack.json"
      echo "CREATE $inst/mmc-pack.json (from Full)"
    else
      echo "WARN $inst: no mmc-pack.json and no Full template"
    fi
  fi

  if [ -f "$idir/mmc-pack.json" ]; then
    python3 - "$idir/mmc-pack.json" "$LOADER_VER" <<'PY'
import json, sys
path, ver = sys.argv[1], sys.argv[2]
data = json.load(open(path))
changed = False
for c in data.get("components", []):
    if c.get("uid") == "net.fabricmc.fabric-loader":
        if c.get("version") != ver or c.get("cachedVersion") != ver:
            c["version"] = ver
            c["cachedVersion"] = ver
            changed = True
if changed:
    json.dump(data, open(path, "w"), indent=4)
    print(f"  loader -> {ver} in {path}")
PY
  fi

  # fabric-api jar swap
  mdir="$idir/minecraft/mods"
  if [ -n "$API_JAR" ] && [ -d "$mdir" ]; then
    old=$(ls "$mdir"/fabric-api-*.jar 2>/dev/null | head -1)
    if [ -z "$old" ]; then
      cp "$API_JAR" "$mdir/"
      echo "  ADD $inst fabric-api: $API_BASE"
    elif ! cmp -s "$API_JAR" "$old"; then
      rm -f "$mdir"/fabric-api-*.jar
      cp "$API_JAR" "$mdir/"
      echo "  UPDATE $inst fabric-api: $(basename "$old") -> $API_BASE"
    fi
  fi

  # Vendored structure/adoption jars (undergroundworlds, dungeons+, Moogs, ...)
  # Only top-level mods (must have fabric.mod.json); nested JiJ libs like
  # kaleido-config stay inside their host jar and must not land in mods/.
  if [ -d "$mdir" ] && [ -d "$ROOT/conversion/vendored" ]; then
    for vj in "$ROOT"/conversion/vendored/*.jar; do
      [ -f "$vj" ] || continue
      case "$vj" in *-sources.jar|*-javadoc.jar) continue ;; esac
      unzip -l "$vj" 2>/dev/null | grep -q 'fabric.mod.json' || continue
      vb=$(basename "$vj")
      if [ ! -f "$mdir/$vb" ] || ! cmp -s "$vj" "$mdir/$vb"; then
        cp "$vj" "$mdir/"
        echo "  VEND $inst $vb"
      fi
    done
  fi

  # datapacks -> world/datapacks + every singleplayer save
  if [ -d "$ROOT/conversion/datapacks" ] && [ -d "$idir/minecraft" ]; then
    dest_roots=("$idir/minecraft/world/datapacks")
    if [ -d "$idir/minecraft/saves" ]; then
      for save in "$idir/minecraft/saves"/*/; do
        [ -d "$save" ] || continue
        dest_roots+=("${save}datapacks")
      done
    fi
    for dest_root in "${dest_roots[@]}"; do
      mkdir -p "$dest_root" 2>/dev/null || continue
      for dp in "$ROOT"/conversion/datapacks/*/; do
        [ -f "${dp}pack.mcmeta" ] || continue
        name=$(basename "$dp")
        dest="$dest_root/$name"
        if [ -d "$dest" ]; then
          rm -rf "$dest"
          cp -R "$dp" "$dest"
        else
          cp -R "$dp" "$dest"
          echo "  ADD datapack $inst/${dest#"$idir/minecraft/"}: $name"
        fi
      done
    done
  fi
done

replaced=0; skipped=0; added=0
# Module roots live under custom-mods/
MODS_ROOT="$ROOT/custom-mods"
for moddir in "$MODS_ROOT"/hearthwind-* "$MODS_ROOT"/letsdo-* "$MODS_ROOT"/smallships "$MODS_ROOT"/villagesandpillages "$MODS_ROOT"/athena "$MODS_ROOT"/chipped "$MODS_ROOT"/dungeonz "$MODS_ROOT"/exposure "$MODS_ROOT"/passable-foliage "$MODS_ROOT"/logbegone "$MODS_ROOT"/entitycollisionfpsfix "$MODS_ROOT"/pockets "$MODS_ROOT"/couplings "$MODS_ROOT"/memoryleakfix "$MODS_ROOT"/async-locator "$MODS_ROOT"/lavender "$MODS_ROOT"/profundis; do
  [ -d "$moddir" ] || continue
  # plain jar only: newest non-sources jar in build/libs
  jar=$(ls -t "$moddir"/build/libs/*.jar 2>/dev/null | grep -v -- "-sources\.jar$" | head -1)
  if [ -z "$jar" ]; then
    echo "SKIP $(basename "$moddir") (not built)"
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

# Self-test every managed instance: entrypoints, mixins, and dependency
# presence are verified statically so a bad deploy fails here, not at launch.
fail=0
for inst in $INSTANCES; do
  python3 "$DIR/verify_prism.py" "$inst" || fail=1
done
[ "$fail" = 0 ] || { echo "prism verify FAILED"; exit 1; }
echo "prism verify: all instances OK"
