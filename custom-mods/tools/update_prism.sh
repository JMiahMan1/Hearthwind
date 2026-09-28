#!/usr/bin/env bash
# Rebuild the Prism Launcher test instances so they match EXACTLY what a user
# gets by importing the built .mrpack files from the GitHub release - one
# instance per published pack, built the way a launcher builds it:
#
#   conversion/build/dist/HearthwindClient-<ver>-mc<mc>.mrpack -> Hearthwind-Client
#     -> modrinth.index.json  (Modrinth jars; copied from build/dist/client/mods,
#                              which was downloaded from those exact URLs)
#     -> overrides/mods/*.jar (our in-house / vendored jars)
#     -> overrides/config, overrides/world/datapacks, overrides/resourcepacks
#   conversion/build/dist/HearthwindServer-<ver>-mc<mc>.mrpack -> Hearthwind-Server
#     -> the same shape, installed from build/dist/server/mods
#
# The instance mods folder is reconciled (added, updated AND pruned) against
# that mrpack, so a mod that leaves the pack leaves the instance too. Without
# this, dropped mods (Terralith, Tectonic, Visuality, Waterfall Particles,
# c2me, ...) lingered and broke test worlds. Exceptions go in prism_keep.txt.
#
# Usage: bash tools/update_prism.sh [--force] [--fresh]
#
#   --force  replace mod jars even while a Minecraft client is live (jar
#            hot-swap corrupts lazily loaded classes - you get
#            "ZipFile invalid LOC header (bad signature)" at the next screen
#            that loads a new class; Prism launches via
#            org.prismlauncher.EntryPoint and Fabric through knot.KnotClient,
#            so all of those patterns are checked).
#   --fresh  delete both managed instances (world data included) and install
#            them again from the packs, i.e. a clean launcher import.
#
# Also: bump mmc-pack.json Fabric Loader to build.conf.json loader_version,
# synthesize mmc-pack.json for instances that lack one (from tools/prism_template
# with the pack's own minecraft/fabric-loader versions), mirror the pack's
# world/datapacks into existing singleplayer saves (test convenience), warn
# when a freshly built module jar is not inside the mrpack, and self-test every
# instance with verify_prism.py.
#
# Instances live at ~/Library/Application Support/PrismLauncher/instances.
# This script only touches files under <instance>/minecraft/ and mmc-pack.json.

set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
# tools/ -> custom-mods/ -> repo root
ROOT="$(cd "$DIR/../.." && pwd)"
cd "$ROOT" || exit 1

INST_ROOT="$HOME/Library/Application Support/PrismLauncher/instances"
INSTANCES="Hearthwind-Client Hearthwind-Server"
DIST_SERVER="$ROOT/conversion/build/dist/server/mods"
TEMPLATE_PACK="$DIR/prism_template/mmc-pack.json"

[ -d "$INST_ROOT" ] || { echo "no Prism installs at $INST_ROOT"; exit 1; }

FORCE=""
FRESH=""
for arg in "$@"; do
  case "$arg" in
    --force) FORCE=1 ;;
    --fresh) FRESH=1 ;;
  esac
done

# Never swap jars under a running game: Fabric loads classes lazily, so the
# first class touched after the file is replaced dies with
# "ZipFile invalid LOC header (bad signature)" (seen as a crash on opening
# the inventory screen). Close the game first; --force overrides.
if [ -z "$FORCE" ] && [ -z "$FRESH" ] && { pgrep -f "net\.minecraft\.client\.main\.Main" >/dev/null 2>&1 \
    || pgrep -f "net\.fabricmc\.devlaunchinjector\.Main" >/dev/null 2>&1 \
    || pgrep -f "org\.prismlauncher\.EntryPoint" >/dev/null 2>&1 \
    || pgrep -f "knot\.KnotClient" >/dev/null 2>&1 \
    || pgrep -f "knot\.KnotServer" >/dev/null 2>&1; }; then
  echo "ERROR: Minecraft is running - refusing to replace mod jars under a live game." >&2
  echo "       Close the game and rerun (use --force to override)." >&2
  exit 1
fi

MRPACK=$(ls -t "$ROOT"/conversion/build/dist/HearthwindClient-*-mc*.mrpack 2>/dev/null | head -1)
if [ -z "$MRPACK" ]; then
  echo "ERROR: no HearthwindClient-*.mrpack - run build_pack.py --server-dir first" >&2
  exit 1
fi
SERVER_MRPACK="${MRPACK/HearthwindClient-/HearthwindServer-}"
DIST_CLIENT="$ROOT/conversion/build/dist/client/mods"

# Which pack (and which materialized dist dir) each instance mirrors.
instance_pack() {
  case "$1" in
    Hearthwind-Client) echo "$MRPACK" ;;
    Hearthwind-Server) echo "$SERVER_MRPACK" ;;
  esac
}
instance_dist() {
  case "$1" in
    Hearthwind-Client) echo "$DIST_CLIENT" ;;
    Hearthwind-Server) echo "$DIST_SERVER" ;;
  esac
}

LOADER_VER=$(python3 -c "import json; print(json.load(open('$ROOT/conversion/build.conf.json'))['targets']['loader_version'])")
[ -n "$LOADER_VER" ] || { echo "ERROR: empty loader_version from build.conf.json"; exit 1; }

echo "loader=$LOADER_VER client_pack=$(basename "$MRPACK")"
[ -f "$SERVER_MRPACK" ] && echo "server_pack=$(basename "$SERVER_MRPACK")"

# Fail fast when a pack itself is incomplete (e.g. packaged before the modules
# were built): instances must mirror a valid pack, not an empty one. This is
# the local counterpart of the CI release gate.
VERIFY_ARGS=("$MRPACK")
[ -f "$SERVER_MRPACK" ] && VERIFY_ARGS+=("$SERVER_MRPACK")
python3 "$DIR/verify_pack.py" "${VERIFY_ARGS[@]}" || {
  echo "ERROR: mrpack failed verification - run ./gradlew build in custom-mods, then build_pack.py --server-dir" >&2
  exit 2
}
# Strict mrpack spec check (index hashes/sizes/env flags, dependency map,
# overrides layout): a launcher must be able to install the pack as shipped.
python3 "$DIR/verify_mrpack.py" "${VERIFY_ARGS[@]}" || {
  echo "ERROR: mrpack failed strict mrpack validation (index hashes, dependencies or overrides layout)" >&2
  exit 2
}

# Keep the vendored jar sources canonical before the pack gets rebuilt.
python3 "$ROOT/conversion/scripts/patch_vendored.py" "$ROOT/conversion" || {
  echo "ERROR: patch_vendored.py failed" >&2
  exit 1
}

# --- per-instance: create if absent, loader bump + mrpack mod set + overrides ---
# The pack needs a Java 26 runtime (e.g. tlc/The Lost Castle ships class file
# version 70), so pin the newest 26 JRE we can find instead of letting Prism
# fall back to whatever it auto-detects.
JAVA_26="$(/usr/libexec/java_home -v 26 2>/dev/null || true)"
if [ -z "$JAVA_26" ]; then
  for candidate in /usr/local/opt/openjdk/libexec/openjdk.jdk/Contents/Home \
                   /Library/Java/JavaVirtualMachines/openjdk.jdk/Contents/Home; do
    if [ -x "$candidate/bin/java" ]; then JAVA_26="$candidate"; break; fi
  done
fi
echo "java26=${JAVA_26:-<none>}"

for inst in $INSTANCES; do
  idir="$INST_ROOT/$inst"
  MRPACK_I=$(instance_pack "$inst")
  DIST_I=$(instance_dist "$inst")
  if [ -z "$MRPACK_I" ] || [ ! -f "$MRPACK_I" ]; then
    echo "SKIP $inst (no pack: $MRPACK_I)"; continue
  fi

  if [ -n "$FRESH" ] && [ -d "$idir" ]; then
    rm -rf "$idir"
    echo "FRESH removed $idir (world data included)"
  fi

  if [ ! -d "$idir/minecraft" ]; then
    mkdir -p "$idir/minecraft/mods" "$idir/minecraft/saves"
  fi

  # Prism only lists an instance when the metadata is there, so write (or
  # repair) it whenever it is missing - a half-created instance dir from an
  # aborted run must not be left without instance.cfg/mmc-pack.json.
  if [ ! -f "$idir/instance.cfg" ] || [ ! -f "$idir/mmc-pack.json" ]; then
    python3 - "$idir" "$inst" "$TEMPLATE_PACK" "$MRPACK_I" "$JAVA_26" <<'PY'
import json
import sys
import uuid
import zipfile

idir, inst, template, mrpack, java26 = sys.argv[1:6]
# The pack is a zip: read the launcher index out of it, never as plain text.
with zipfile.ZipFile(mrpack) as zf:
    index = json.loads(zf.read("modrinth.index.json"))
deps = index.get("dependencies", {})

java_line = f"JavaPath={java26}/bin/java\n" if java26 else ""
cfg = f"""[General]
AutomaticJava=false
ConfigVersion=1.3
IconKey=default
InstanceType=OneSix
JavaArchitecture=64
JoinServerOnLaunch=false
ManagedPack=false
MaxMemAlloc=4096
OverrideJavaLocation=true
{java_line}name={inst}
uuid={uuid.uuid4().hex}
lastTimePlayed=0
totalTimePlayed=0
"""
if not __import__("os").path.exists(f"{idir}/instance.cfg"):
    with open(f"{idir}/instance.cfg", "w") as f:
        f.write(cfg)

if template and __import__("os").path.isfile(template):
    with open(template) as f:
        pack = json.load(f)
else:
    pack = {"formatVersion": 1, "components": []}
if not pack.get("uid"):
    pack["uid"] = "hearthwind"
    pack["name"] = inst
    pack["version"] = index.get("versionId", "1.0.0")
    pack["pack_type"] = "Modrinth"

wanted = {c.get("uid"): c for c in pack.get("components", [])}
mc = deps.get("minecraft")
loader = deps.get("fabric-loader")
wanted["net.minecraft"] = {
    "uid": "net.minecraft",
    "version": mc,
    "cachedVersion": mc,
}
wanted["net.fabricmc.fabric-loader"] = {
    "uid": "net.fabricmc.fabric-loader",
    "version": loader,
    "cachedVersion": loader,
}
order = ["net.minecraft", "net.fabricmc.intermediary", "org.lwjgl3", "net.fabricmc.fabric-loader"]
names = [u for u in order if u in wanted] + [u for u in sorted(wanted) if u not in order]
pack["components"] = [wanted[u] for u in names]
if not __import__("os").path.exists(f"{idir}/mmc-pack.json"):
    with open(f"{idir}/mmc-pack.json", "w") as f:
        json.dump(pack, f, indent=4)
print(f"  CREATE {inst}: instance.cfg + mmc-pack.json (minecraft {mc}, fabric-loader {loader})")
PY
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
  if [ -d "$mdir" ] && [ -d "$DIST_I" ]; then
    python3 - "$MRPACK_I" "$DIST_I" "$mdir" "$DIR/prism_keep.txt" "$inst" <<'PY'
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

# A jar rewritten while the game was reading it can look fine by size but
# carry truncated entries ("invalid LOC header"). Refuse to leave a broken
# instance behind: test every zip that should be playable.
corrupt = []
for name in sorted(expected):
    jar = target / name
    try:
        with zipfile.ZipFile(jar) as archive:
            if archive.testzip() is not None:
                corrupt.append(name)
    except (OSError, zipfile.BadZipFile):
        corrupt.append(name)
if corrupt:
    print(f"ERROR {inst}: corrupt jars after sync: {', '.join(corrupt)}")
    sys.exit(3)

print(f"  {inst}: mods {len(expected)} expected, {added} added, {updated} updated, {pruned} pruned, {kept} kept")
PY
    [ $? -eq 0 ] || { echo "ERROR: mod sync failed for $inst" >&2; exit 1; }
  fi

  # Extract overrides exactly like Prism does on import (config, datapacks,
  # resourcepacks, resources, ...). overrides/mods is handled above.
  if [ -d "$idir/minecraft" ]; then
    python3 - "$MRPACK_I" "$idir/minecraft" "$inst" <<'PY'
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
