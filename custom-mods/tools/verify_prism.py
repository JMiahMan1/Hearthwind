#!/usr/bin/env python3
"""Static verification for a Prism instance's mods dir.

Catches the class of breakage that crashes the client at startup:
  - fabric.mod.json missing/unparseable, or with unexpanded ${...} versions
  - entrypoint classes (main/client/server/...) absent from the jar
  - mixin config classes absent from the jar
  - declared `depends` ids with no provider jar in the same mods dir

Usage: python3 tools/verify_prism.py [InstanceName] (default Hearthwind-Dev-Client)
Exit nonzero on any error.
"""
import json
import sys
import zipfile
from pathlib import Path

INST = sys.argv[1] if len(sys.argv) > 1 else "Hearthwind-Dev-Client"
MODS = Path.home() / "Library/Application Support/PrismLauncher/instances" / INST / "minecraft/mods"

# dependency ids always provided by the loader/environment, not by a mod jar
BUILTIN = {"minecraft", "java", "fabricloader", "fabric-api", "mixinextras"}


def read_json(zf, name):
    return json.loads(zf.read(name), strict=False)


def nested_ids(zf):
    """Mod ids embedded via Fabric Jar-in-Jar."""
    ids = set()
    try:
        fmj = read_json(zf, "fabric.mod.json")
    except (KeyError, json.JSONDecodeError):
        return ids
    for entry in fmj.get("jars") or []:
        f = entry.get("file") if isinstance(entry, dict) else None
        if not f:
            continue
        for cand in (f, "META-INF/jars/" + f):
            try:
                data = zf.read(cand)
            except KeyError:
                continue
            try:
                with zipfile.ZipFile(__import__("io").BytesIO(data)) as nested:
                    try:
                        ids.add(read_json(nested, "fabric.mod.json").get("id", ""))
                    except (KeyError, json.JSONDecodeError):
                        pass
            except zipfile.BadZipFile:
                pass
    return ids - {""}

errors = []
checked = 0


def cls_path(name):
    return name.replace(".", "/") + ".class"


for jar in sorted(MODS.glob("*.jar")):
    if jar.name.endswith("-sources.jar"):
        continue
    try:
        zf = zipfile.ZipFile(jar)
    except zipfile.BadZipFile:
        errors.append(f"{jar.name}: not a valid zip")
        continue
    names = set(zf.namelist())
    try:
        fmj = read_json(zf, "fabric.mod.json")
    except KeyError:
        errors.append(f"{jar.name}: fabric.mod.json missing")
        continue
    except json.JSONDecodeError as e:
        errors.append(f"{jar.name}: fabric.mod.json invalid: {e}")
        continue
    checked += 1
    modid = fmj.get("id", jar.name)
    ver = str(fmj.get("version", ""))
    if "${" in ver:
        errors.append(f"{modid}: unexpanded version {ver!r}")
    for stage, eps in (fmj.get("entrypoints") or {}).items():
        eps = eps if isinstance(eps, list) else [eps]
        for ep in eps:
            if isinstance(ep, dict):
                ep = ep.get("value", "")
            ep = ep.split("::")[0]  # strip method reference
            if not ep:
                continue
            if cls_path(ep) not in names:
                errors.append(f"{modid}: entrypoint {stage} class missing: {ep}")
    for mixin_entry in fmj.get("mixins") or []:
        mixin_file = mixin_entry.get("config", "") if isinstance(mixin_entry, dict) else mixin_entry
        if not mixin_file:
            continue
        try:
            mixins = read_json(zf, mixin_file)
        except KeyError:
            errors.append(f"{modid}: mixin config missing: {mixin_file}")
            continue
        pkg = mixins.get("package", "")
        for key in ("mixins", "client", "server"):
            for m in mixins.get(key) or []:
                if cls_path(pkg + "." + m) not in names:
                    errors.append(f"{modid}: mixin class missing: {pkg}.{m}")
    aw = fmj.get("accessWidener")
    if aw and aw not in names:
        errors.append(f"{modid}: accessWidener missing: {aw}")

# dependency presence across the whole mods dir (plus Jar-in-Jar contents)
provided = set()
for jar in sorted(MODS.glob("*.jar")):
    if jar.name.endswith("-sources.jar"):
        continue
    try:
        with zipfile.ZipFile(jar) as zf:
            try:
                fmj = read_json(zf, "fabric.mod.json")
            except (KeyError, json.JSONDecodeError):
                continue
            if fmj.get("id"):
                provided.add(fmj["id"])
            for pid in (fmj.get("provides") or []):
                provided.add(pid)
            provided |= nested_ids(zf)
    except zipfile.BadZipFile:
        continue
for jar in sorted(MODS.glob("*.jar")):
    if jar.name.endswith("-sources.jar"):
        continue
    try:
        with zipfile.ZipFile(jar) as zf:
            try:
                fmj = read_json(zf, "fabric.mod.json")
            except (KeyError, json.JSONDecodeError):
                continue
            for dep in (fmj.get("depends") or {}):
                if dep not in BUILTIN and dep not in provided and not dep.startswith("fabric-"):
                    errors.append(f"{fmj.get('id', jar.name)}: unsatisfied dependency: {dep}")
    except zipfile.BadZipFile:
        continue

print(f"verify_prism: checked {checked} jars in {INST}, {len(provided)} mod ids provided")
for e in errors:
    print("ERROR " + e)
sys.exit(1 if errors else 0)
