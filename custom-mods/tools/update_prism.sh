#!/usr/bin/env bash
# Rebuild the Prism Launcher test instances so they match EXACTLY what a user
# gets by importing the built .mrpack from the GitHub release:
#
#   conversion/build/dist/HearthwindClient-<ver>-mc<mc>.mrpack
#     -> modrinth.index.json  (Modrinth jars; copied from build/dist/client/mods,
#                              which was downloaded from those exact URLs)
#     -> overrides/mods/*.jar (our in-house / vendored jars)
#     -> overrides/config, overrides/world/datapacks, overrides/resourcepacks
#
# The instance mods folder is reconciled (added, updated AND pruned) against
# that mrpack, so a mod that leaves the pack leaves the instance too. Without
# this, dropped mods (Terralith, Tectonic, Visuality, Waterfall Particles,
# c2me, ...) lingered and broke test worlds. Exceptions go in prism_keep.txt.
#
# Usage: bash tools/update_prism.sh [--force]
#
# Refuses to run while a Minecraft client is live (jar hot-swap corrupts
# lazily loaded classes); pass --force to override.
#
# Also: bump mmc-pack.json Fabric Loader to build.conf.json loader_version,
# synthesize mmc-pack.json for instances that lack one, mirror the pack's
# world/datapacks into existing singleplayer saves (test convenience), and
# warn when a freshly built module jar is not yet inside the mrpack (forgot
# to run build_pack.py --server-dir).
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

[ -d "$INST_ROOT" ] || { echo "no Prism installs at $INST_ROOT"; exit 1; }

# Never swap jars under a running game: Fabric loads classes lazily, so the
# first class touched after the file is replaced dies with
# "ZipFile invalid LOC header (bad signature)" (seen as a crash on opening
# the inventory screen). Close the game first; --force overrides.
FORCE=""
for arg in "$@"; do [ "$arg" = "--force" ] && FORCE=1; done
if [ -z "$FORCE" ] && { pgrep -f "net\.minecraft\.client\.main\.Main" >/dev/null 2>&1 \
    || pgrep -f "net\.fabricmc\.devlaunchinjector\.Main" >/dev/null 2>&1; }; then
  echo "ERROR: Minecraft is running - refusing to replace mod jars under a live game." >&2
  echo "       Close the game and rerun (use --force to override)." >&2
  exit 1
fi

MRPACK=$(ls -t "$ROOT"/conversion/build/dist/HearthwindClient-*-mc*.mrpack 2>/dev/null | head -1)
if [ -z "$MRPACK" ]; then
  echo "ERROR: no HearthwindClient-*.mrpack - run build_pack.py --server-dir first" >&2
  exit 1
fi
DIST_CLIENT="$ROOT/conversion/build/dist/client/mods"

LOADER_VER=$(python3 -c "import json; print(json.load(open('$ROOT/conversion/build.conf.json'))['targets']['loader_version'])")
[ -n "$LOADER_VER" ] || { echo "ERROR: empty loader_version from build.conf.json"; exit 1; }

echo "loader=$LOADER_VER mrpack=$(basename "$MRPACK")"

# Keep the vendored jar sources canonical before the pack gets rebuilt.
python3 "$ROOT/conversion/scripts/patch_vendored.py" "$ROOT/conversion" || {
  echo "ERROR: patch_vendored.py failed" >&2
  exit 1
}

# --- per-instance: loader bump + mrpack mod set + overrides ---
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

  mdir="$idir/minecraft/mods"
  if [ -d "$mdir" ] && [ -d "$DIST_CLIENT" ]; then
    python3 - "$MRPACK" "$DIST_CLIENT" "$mdir" "$DIR/prism_keep.txt" "$inst" <<'PY'
import json
import shutil
import sys
import zipfile
from pathlib import Path

mrpack, dist, target, keep_path, inst = sys.argv[1:6]
dist = Path(dist)
target = Path(target)

keep = set()
kp = Path(keep_path)
if kp.is_file():
    for line in kp.read_text().splitlines():
        line = line.split("#", 1)[0].strip()
        if line:
            keep.add(line)


def read_meta_id(jar):
    try:
        with zipfile.ZipFile(jar) as archive:
            raw = archive.read("fabric.mod.json").decode("utf-8", "replace")
        try:
            return json.loads(raw).get("id")
        except ValueError:
            return json.loads(raw, strict=False).get("id")
    except (KeyError, OSError, ValueError, zipfile.BadZipFile):
        return None


# Expected instance mods = mrpack index jars + mrpack override jars.
expected = set()
with zipfile.ZipFile(mrpack) as z:
    index = json.loads(z.read("modrinth.index.json"))
    for f in index.get("files", []):
        path = f.get("path", "")
        if path.startswith("mods/") and path.endswith(".jar"):
            expected.add(Path(path).name)
    for name in z.namelist():
        if name.startswith("overrides/mods/") and name.endswith(".jar"):
            expected.add(Path(name).name)

available = {p.name for p in dist.glob("*.jar")}
missing = sorted(expected - available)
if missing:
    print(f"ERROR {inst}: mrpack jars missing from {dist}: {', '.join(missing)}")
    sys.exit(2)

pruned = kept = 0
for jar in sorted(target.glob("*.jar")):
    if jar.name in expected:
        continue
    if jar.name in keep or read_meta_id(jar) in keep:
        kept += 1
        continue
    jar.unlink()
    pruned += 1
    print(f"  PRUNE {inst}/{jar.name}")

added = updated = 0
for name in sorted(expected):
    src = dist / name
    dst = target / name
    if dst.exists() and dst.read_bytes() == src.read_bytes():
        continue
    existed = dst.exists()
    shutil.copy2(src, dst)
    if existed:
        updated += 1
    else:
        added += 1
        print(f"  ADD {inst}/{name}")

print(f"  {inst}: mods {len(expected)} expected, {added} added, {updated} updated, {pruned} pruned, {kept} kept")
PY
    [ $? -eq 0 ] || { echo "ERROR: mod sync failed for $inst" >&2; exit 1; }
  fi

  # Extract overrides exactly like Prism does on import (config, datapacks,
  # resourcepacks, defaultconfigs, ...). overrides/mods is handled above.
  if [ -d "$idir/minecraft" ]; then
    python3 - "$MRPACK" "$idir/minecraft" "$inst" <<'PY'
import sys
import zipfile
from pathlib import Path

mrpack, mc_dir, inst = sys.argv[1:4]
mc = Path(mc_dir)
changed = 0
with zipfile.ZipFile(mrpack) as z:
    for name in z.namelist():
        if not name.startswith("overrides/") or name.endswith("/"):
            continue
        rel = name[len("overrides/"):]
        if rel.startswith("mods/"):
            continue
        dest = mc / rel
        data = z.read(name)
        if dest.exists() and dest.read_bytes() == data:
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(data)
        changed += 1
print(f"  {inst}: overrides {changed} file(s) synced from the mrpack")
PY
  fi

  # Test convenience: existing singleplayer saves read saves/<level>/datapacks,
  # not world/datapacks, so mirror the pack datapacks into each save.
  if [ -d "$idir/minecraft/world/datapacks" ] && [ -d "$idir/minecraft/saves" ]; then
    for save in "$idir/minecraft/saves"/*/; do
      [ -d "$save" ] || continue
      for dp in "$idir/minecraft/world/datapacks"/*/; do
        [ -f "$dp/pack.mcmeta" ] || continue
        name=$(basename "$dp")
        dest="$save/datapacks/$name"
        mkdir -p "$save/datapacks"
        rm -rf "$dest"
        cp -R "$dp" "$dest"
      done
    done
  fi
done

# Warn about freshly built module jars that are not inside the mrpack yet:
# the instance now mirrors the pack, so a jar missing here is a forgotten
# build_pack.py --server-dir (the "live-tested code != released code" trap).
python3 - "$MRPACK" "$ROOT" <<'PY'
import sys
import zipfile
from pathlib import Path

mrpack = sys.argv[1]
root = Path(sys.argv[2])
with zipfile.ZipFile(mrpack) as z:
    packed = {
        Path(n).name
        for n in z.namelist()
        if n.startswith("overrides/mods/") and n.endswith(".jar")
    }
stale = []
for jar in sorted((root / "custom-mods").glob("*/build/libs/*.jar")):
    if jar.name.endswith(("-sources.jar", "-javadoc.jar")):
        continue
    if jar.name not in packed:
        stale.append(jar.name)
if stale:
    print("STALE-BUILD (not in the mrpack - run build_pack.py --server-dir):")
    for name in stale:
        print(f"  {name}")
PY

# Self-test every managed instance: entrypoints, mixins, and dependency
# presence are verified statically so a bad deploy fails here, not at launch.
fail=0
for inst in $INSTANCES; do
  python3 "$DIR/verify_prism.py" "$inst" || fail=1
done
[ "$fail" = 0 ] || { echo "prism verify FAILED"; exit 1; }
echo "prism verify: all instances OK"
