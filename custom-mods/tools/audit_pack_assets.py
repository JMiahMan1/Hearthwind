#!/usr/bin/env python3
"""Pack-wide asset audit: does every model in the client pack resolve?

`validate_models.py` only walks models reachable from OUR modules' own
blockstates and item definitions, because it can read our Java sources to
decide which blockstates are actually registered. For the other ~140 jars
there is no source to read, so this audit scans EVERY model, blockstate and
item definition in the pack. That is deliberately broader: an unreachable
model that points at a missing texture is not a rendering bug, but it is
still a broken file, and the report says so rather than hiding it.

Sources indexed, in the order the client resolves them:

  1. the pack's resource packs (`conversion/overrides/resourcepacks/*`), which
     override everything below;
  2. every jar in `conversion/build/dist/client/mods/`;
  3. the loom-cached merged vanilla client jar.

Usage:

    python3 custom-mods/tools/audit_pack_assets.py [--mods-dir DIR]
                                                  [--vanilla-jar PATH]
                                                  [--json OUT]
Exit code is 0 even when problems are found - this is a diagnostic, not a
gate. Wire it into CI only once the third-party backlog is cleared.
"""

from __future__ import annotations

import argparse
import collections
import glob
import json
import os
import sys
import zipfile
from pathlib import Path

MOD_ROOT = Path(__file__).resolve().parents[1]
REPO = MOD_ROOT.parent
DEFAULT_MODS_DIR = REPO / "conversion/build/dist/client/mods"
DEFAULT_PACKS_DIR = REPO / "conversion/overrides/resourcepacks"
VANILLA_JAR_GLOB = str(
    MOD_ROOT
    / ".gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/26.2/minecraft-merged-*-26.2.jar"
)

# Keys a model uses to point at a texture. `#foo` is a local reference that
# some model in the parent chain must define; `ns:path` (or a bare path, which
# means the model's own namespace) is a concrete file.
TEXTURE_KEYS = ("texture", "particle", "layer0")


def parse_id(value: str, default_ns: str) -> tuple[str, str]:
    ns, _, path = value.partition(":")
    if not path:
        return default_ns, value
    return ns, path


class Index:
    """Everything the client can see: models by id, textures by id."""

    def __init__(self) -> None:
        self.models: dict[str, dict] = {}
        self.textures: set[str] = set()
        self.blockstates: dict[str, dict] = {}
        self.items: dict[str, dict] = {}
        # texture id -> the mod/jar it came from, for the report
        self.texture_owner: dict[str, str] = {}
        self.model_owner: dict[str, str] = {}
        self.sources: list[str] = []

    def add_jar(self, jar: Path, label: str) -> int:
        added = 0
        with zipfile.ZipFile(jar) as z:
            for name in z.namelist():
                if not name.startswith("assets/"):
                    continue
                rest = name[len("assets/") :]
                ns, _, tail = rest.partition("/")
                if not ns or not tail:
                    continue
                try:
                    if tail.startswith("models/") and name.endswith(".json"):
                        key = f"{ns}:{tail[len('models/'):-len('.json')]}"
                        self.models.setdefault(key, json.loads(z.read(name)))
                        self.model_owner.setdefault(key, label)
                        added += 1
                    elif tail.startswith("blockstates/") and name.endswith(".json"):
                        self.blockstates.setdefault(
                            f"{ns}:{tail[len('blockstates/'):-len('.json')]}",
                            json.loads(z.read(name)),
                        )
                        added += 1
                    elif tail.startswith("items/") and name.endswith(".json"):
                        self.items.setdefault(
                            f"{ns}:{tail[len('items/'):-len('.json')]}",
                            json.loads(z.read(name)),
                        )
                        added += 1
                    elif tail.startswith("textures/") and name.endswith(".png"):
                        key = f"{ns}:{tail[len('textures/'):-len('.png')]}"
                        self.textures.add(key)
                        self.texture_owner.setdefault(key, label)
                        added += 1
                except (json.JSONDecodeError, KeyError):
                    continue
        self.sources.append(f"{label} ({added} assets)")
        return added

    def add_dir(self, root: Path, label: str) -> int:
        """A resource pack on disk, which the client layers over the jars."""
        added = 0
        for name in root.rglob("assets/*/**"):
            if not name.is_file():
                continue
            rel = name.relative_to(root / "assets").as_posix()
            ns, _, tail = rel.partition("/")
            if not tail:
                continue
            try:
                if tail.startswith("models/") and name.suffix == ".json":
                    key = f"{ns}:{tail[len('models/'):-len('.json')]}"
                    self.models[key] = json.loads(name.read_text())
                    self.model_owner[key] = label
                    added += 1
                elif tail.startswith("blockstates/") and name.suffix == ".json":
                    self.blockstates[f"{ns}:{tail[len('blockstates/'):-len('.json')]}"] = json.loads(
                        name.read_text()
                    )
                    added += 1
                elif tail.startswith("items/") and name.suffix == ".json":
                    self.items[f"{ns}:{tail[len('items/'):-len('.json')]}"] = json.loads(
                        name.read_text()
                    )
                    added += 1
                elif tail.startswith("textures/") and name.suffix == ".png":
                    key = f"{ns}:{tail[len('textures/'):-len('.png')]}"
                    self.textures.add(key)
                    self.texture_owner[key] = label
                    added += 1
            except json.JSONDecodeError:
                continue
        self.sources.append(f"{label} ({added} assets)")
        return added


def face_textures(model: dict):
    for element in model.get("elements") or []:
        if not isinstance(element, dict):
            continue
        for face in (element.get("faces") or {}).values():
            if isinstance(face, dict) and isinstance(face.get("texture"), str):
                yield face["texture"]


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--mods-dir", default=str(DEFAULT_MODS_DIR))
    ap.add_argument("--packs-dir", default=str(DEFAULT_PACKS_DIR))
    ap.add_argument("--vanilla-jar", default=None)
    ap.add_argument("--json", default=None, help="also write a machine-readable report")
    args = ap.parse_args()

    mods_dir = Path(args.mods_dir)
    if not mods_dir.is_dir():
        print(
            f"ERROR: {mods_dir} does not exist; run\n"
            "  python3 conversion/scripts/build_pack.py --server-dir",
            file=sys.stderr,
        )
        return 2

    vanilla = sorted(glob.glob(args.vanilla_jar or VANILLA_JAR_GLOB))
    if not vanilla:
        print("ERROR: no loom-cached client jar; run ./gradlew build first", file=sys.stderr)
        return 2

    index = Index()
    for pack in sorted(Path(args.packs_dir).glob("*")):
        if (pack / "assets").is_dir():
            index.add_dir(pack, f"resourcepack {pack.name}")
    index.add_jar(Path(vanilla[-1]), "minecraft (vanilla)")
    jars = sorted(mods_dir.glob("*.jar"))
    for jar in jars:
        index.add_jar(jar, jar.name)
    for vendor in sorted((REPO / "conversion/vendored").glob("*.jar")):
        index.add_jar(vendor, f"vendored {vendor.name}")

    # ---------------------------------------------------------------- models
    missing_texture: collections.Counter = collections.Counter()
    missing_texture_where: dict[str, str] = {}
    missing_model: collections.Counter = collections.Counter()
    missing_model_where: dict[str, str] = {}
    dangling_refs: collections.Counter = collections.Counter()
    dangling_where: dict[str, str] = {}
    checked_models = 0

    def model_chain(model_id: str, seen: set[str], out: list[tuple[str, dict]]):
        if model_id in seen:
            return
        seen.add(model_id)
        data = index.models.get(model_id)
        if data is None:
            return
        out.append((model_id, data))
        parent = data.get("parent")
        if isinstance(parent, str):
            model_chain(parent, seen, out)

    def report_texture(ref: str, ns: str, where: str) -> None:
        missing_texture[ns] += 1
        missing_texture_where.setdefault(f"{ns}|{ref}", where)

    for model_id, model in index.models.items():
        ns, _ = parse_id(model_id, "minecraft")
        checked_models += 1
        chain: list[tuple[str, dict]] = []
        model_chain(model_id, set(), chain)

        defs: dict[str, str] = {}
        for mid, data in chain:
            for key, value in (data.get("textures") or {}).items():
                if isinstance(value, str):
                    defs.setdefault(key, value)

        refs: set[str] = set()
        for mid, data in chain:
            for value in (data.get("textures") or {}).values():
                if isinstance(value, str):
                    refs.add(value)
            refs.update(face_textures(data))

        parent = model.get("parent")
        if isinstance(parent, str):
            pid = parent if ":" in parent else f"{ns}:{parent}"
            pns, ppath = parse_id(pid, ns)
            if pns == "minecraft" and ppath.startswith("builtin/"):
                pass
            elif pid not in index.models:
                missing_model[ns] += 1
                missing_model_where.setdefault(pid, f"parent of {model_id}")

        for value in refs:
            if not value.startswith("#"):
                tns, tpath = parse_id(value, ns)
                if tns == "minecraft" and tpath.startswith("#"):
                    continue
                # Armor trims are equipment overlay layers registered by
                # TrimMaterial, not standalone PNGs; vanilla ships only some of
                # them and the game synthesises the rest. `missingno` is the
                # missing-texture marker itself and `all` is a sentinel. None of
                # these are loadable files, so treating them as absent textures
                # is a false positive.
                if tns == "minecraft" and (
                    tpath.startswith("trims/") or tpath in ("missingno", "all")
                ):
                    continue
                key = f"{tns}:{tpath}"
                if key not in index.textures:
                    report_texture(key, tns, f"model {model_id}")
                continue
            # `#name` is deliberately NOT reported. Vanilla ships hundreds of
            # dormant *template* models (block/template_farmland and friends)
            # whose faces point at `#dirt`, `#top`, `#all` with nothing in the
            # parent chain defining them - they are never rendered, because the
            # game binds those variables at runtime, and the game does not
            # complain about them. Reporting them produced 1,400-odd
            # false positives on the first run, which is why this audit only
            # reports CONCRETE `ns:path` references: a missing PNG is a real
            # bug whether or not the model is reachable, and that is the
            # checkerboard the player sees.
            if value[1:] not in defs:
                dangling_refs[ns] += 1
                dangling_where.setdefault(f"{ns}|{value}", f"model {model_id}")

    # ------------------------------------------------- blockstates and items
    def check_model_ref(ref: str, ns: str, where: str) -> None:
        if not isinstance(ref, str) or not ref:
            return
        mid = ref if ":" in ref else f"{ns}:{ref}"
        mns, mpath = parse_id(mid, ns)
        if mns == "minecraft" and mpath.startswith("builtin/"):
            return
        if mid not in index.models:
            missing_model[ns] += 1
            missing_model_where.setdefault(mid, where)

    for bs_id, data in index.blockstates.items():
        ns, _ = parse_id(bs_id, "minecraft")
        for variant in (data.get("variants") or {}).values():
            for entry in variant if isinstance(variant, list) else [variant]:
                if isinstance(entry, dict):
                    check_model_ref(entry.get("model"), ns, f"blockstate {bs_id}")
        for part in data.get("multipart") or []:
            apply = part.get("apply") if isinstance(part, dict) else None
            for entry in apply if isinstance(apply, list) else [apply]:
                if isinstance(entry, dict):
                    check_model_ref(entry.get("model"), ns, f"blockstate {bs_id}")

    def walk_items(node, ns, item_id):
        if isinstance(node, dict):
            for key, value in node.items():
                if key == "model" and isinstance(value, dict):
                    check_model_ref(value.get("id"), ns, f"item {item_id}")
                elif key in ("models", "model") and isinstance(value, list):
                    for entry in value:
                        if isinstance(entry, str):
                            check_model_ref(entry, ns, f"item {item_id}")
                        else:
                            walk_items(entry, ns, item_id)
                elif key == "models" and isinstance(value, dict):
                    walk_items(value, ns, item_id)
                else:
                    walk_items(value, ns, item_id)
        elif isinstance(node, list):
            for entry in node:
                walk_items(entry, ns, item_id)

    for item_id, data in index.items.items():
        ns, _ = parse_id(item_id, "minecraft")
        walk_items(data, ns, item_id)

    total_missing_textures = sum(missing_texture.values())
    total_missing_models = sum(missing_model.values())

    print(f"indexed {len(index.models)} models, {len(index.textures)} textures, "
          f"{len(index.blockstates)} blockstates, {len(index.items)} item definitions")
    print(f"checked {checked_models} models for texture references")
    print()
    if total_missing_textures:
        print(f"MISSING TEXTURES ({total_missing_textures} references)")
        for ns, count in missing_texture.most_common():
            owner = index.model_owner.get(f"{ns}:", "third-party")
            print(f"  {ns}: {count}   (models from {owner})")
        print()
        print("  every distinct reference:")
        for key, where in sorted(missing_texture_where.items()):
            print(f"    {key}   <- {where}")
    else:
        print("MISSING TEXTURES: none")
    print()
    if total_missing_models:
        print(f"MISSING MODELS ({total_missing_models} references)")
        for ns, count in missing_model.most_common():
            print(f"  {ns}: {count}")
        print()
        for mid, where in sorted(missing_model_where.items()):
            print(f"    {mid}   <- {where}")
    else:
        print("MISSING MODELS: none")

    if dangling_refs:
        print()
        print(f"(not reported: {sum(dangling_refs.values())} dangling #ref texture variables "
              f"across {len(dangling_where)} names, mostly vanilla's dormant template models; "
              f"the game binds those at runtime)")
    if args.json:
        Path(args.json).write_text(
            json.dumps(
                {
                    "models": len(index.models),
                    "textures": len(index.textures),
                    "missing_textures": dict(missing_texture),
                    "missing_models": dict(missing_model),
                    "missing_texture_refs": missing_texture_where,
                    "missing_model_refs": missing_model_where,
                    "dangling_texture_vars": dict(dangling_refs),
                },
                indent=2,
                sort_keys=True,
            )
        )
    return 0


if __name__ == "__main__":
    sys.exit(main())