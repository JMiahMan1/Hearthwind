#!/usr/bin/env python3
"""Validate every model reachable from our item definitions and blockstates.

Catches the failure modes that log warnings and render as missing
textures/models in game:

  1. a concrete texture reference whose PNG is absent (own namespace or
     minecraft:);
  2. a `#name` texture reference that nothing in the parent chain defines;
  3. a `parent` / blockstate / item-definition model reference whose model
     JSON is absent (e.g. the removed minecraft:item/template_spawn_egg).

Reachability mirrors the game: only models referenced from a blockstate or
an item definition are scanned, so dormant files produce no false
positives. Vanilla assets come from the loom-cached merged client jar, so
run this after ./gradlew build:

    python3 custom-mods/tools/validate_models.py [--client-jar PATH]
"""

from __future__ import annotations

import argparse
import glob
import json
import os
import re
import sys
import zipfile
from pathlib import Path

MOD_ROOT = Path(__file__).resolve().parents[1]
RESOURCE_ROOTS = sorted(glob.glob(str(MOD_ROOT / "*/src/main/resources/assets/*")))
VANILLA_JAR_GLOB = str(
    MOD_ROOT
    / ".gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/26.2/minecraft-merged-*-26.2.jar"
)
ID_RE = re.compile(r"^[a-z0-9_.-]+:[a-z0-9_/.-]+$")


def parse_id(value: str, default_ns: str = "minecraft") -> tuple[str, str]:
    ns, _, path = value.partition(":")
    if not path:
        return default_ns, value
    return ns, path


def load_vanilla(client_jar: Path):
    models: dict[str, dict] = {}
    textures: set[str] = set()
    with zipfile.ZipFile(client_jar) as jar:
        for name in jar.namelist():
            if name.startswith("assets/minecraft/models/") and name.endswith(".json"):
                models[name[len("assets/minecraft/models/") : -len(".json")]] = json.loads(
                    jar.read(name)
                )
            elif name.startswith("assets/minecraft/textures/") and name.endswith(".png"):
                textures.add(name[len("assets/minecraft/textures/") : -len(".png")])
    return models, textures


def load_ours():
    models: dict[str, dict] = {}
    textures: set[str] = set()
    for asset_root in RESOURCE_ROOTS:
        ns = os.path.basename(asset_root)
        root = Path(asset_root)
        for path in root.rglob("models/**/*.json"):
            rel = path.relative_to(root).as_posix()
            models[f"{ns}:{rel[len('models/') : -len('.json')]}"] = json.loads(path.read_text())
        for path in root.rglob("textures/**/*.png"):
            textures.add(path.relative_to(root).as_posix()[len("textures/") : -len(".png")])
    return models, textures


def collect_roots() -> set[str]:
    """Model ids referenced by blockstates and item definitions.

    Vendored ports (chipped, letsdo-*, ...) ship many dormant blockstates
    for variants they never register; the game never loads those, so a
    blockstate only counts as a root when its name appears in the module's
    Java sources (registration literals). Item definitions always count.
    """
    roots: set[str] = set()
    sources_cache: dict[str, str] = {}
    for asset_root in RESOURCE_ROOTS:
        ns = os.path.basename(asset_root)
        root = Path(asset_root)
        module_dir = root.parents[2]

        def module_sources() -> str:
            key = str(module_dir)
            if key not in sources_cache:
                sources_cache[key] = " ".join(
                    p.read_text(errors="ignore") for p in module_dir.rglob("*.java")
                )
            return sources_cache[key]

        for path in root.rglob("blockstates/**/*.json"):
            if path.stem not in module_sources():
                continue
            data = json.loads(path.read_text())
            for variant in (data.get("variants") or {}).values():
                for entry in variant if isinstance(variant, list) else [variant]:
                    if isinstance(entry, dict) and isinstance(entry.get("model"), str):
                        model = entry["model"]
                        roots.add(model if ":" in model else f"{ns}:{model}")
            for part in data.get("multipart") or []:
                apply = part.get("apply") if isinstance(part, dict) else None
                for entry in apply if isinstance(apply, list) else [apply]:
                    if isinstance(entry, dict) and isinstance(entry.get("model"), str):
                        model = entry["model"]
                        roots.add(model if ":" in model else f"{ns}:{model}")
        for path in root.rglob("items/**/*.json"):

            def walk(node):
                if isinstance(node, dict):
                    for key, value in node.items():
                        if key == "model" and isinstance(value, str) and ID_RE.match(value):
                            roots.add(value)
                        elif key == "models" and isinstance(value, list):
                            for entry in value:
                                if isinstance(entry, str) and ID_RE.match(entry):
                                    roots.add(entry)
                                else:
                                    walk(entry)
                        else:
                            walk(value)
                elif isinstance(node, list):
                    for entry in node:
                        walk(entry)

            walk(json.loads(path.read_text()))
    return roots


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--client-jar", default=None)
    args = parser.parse_args()

    jars = sorted(glob.glob(args.client_jar or VANILLA_JAR_GLOB))
    if not jars:
        print("ERROR: no loom-cached client jar; run ./gradlew build first", file=sys.stderr)
        return 2
    vanilla_models, vanilla_textures = load_vanilla(Path(jars[-1]))
    our_models, our_textures = load_ours()
    roots = collect_roots()

    problems: list[str] = []
    checked: set[str] = set()

    def lookup(model_id: str) -> dict | None:
        ns, path = parse_id(model_id)
        if ns == "minecraft":
            if path.startswith("builtin/"):
                return {}
            return vanilla_models.get(path)
        return our_models.get(model_id)

    def chain(model_id: str, seen: set[str], out: list[tuple[str, dict]]):
        if model_id in seen:
            return
        seen.add(model_id)
        data = lookup(model_id)
        if data is None:
            return
        out.append((model_id, data))
        parent = data.get("parent")
        if isinstance(parent, str):
            chain(parent, seen, out)

    def face_refs(data: dict):
        for element in data.get("elements") or []:
            for face in (element.get("faces") or {}).values():
                if isinstance(face, dict) and isinstance(face.get("texture"), str):
                    yield face["texture"]

    def validate_root(root_id: str, origin: str):
        models: list[tuple[str, dict]] = []
        chain(root_id, set(), models)
        if not models:
            problems.append(f"missing model: {root_id}  ({origin})")
            return
        defs: dict[str, str] = {}
        refs: set[str] = set()
        for model_id, data in models:
            for key, value in (data.get("textures") or {}).items():
                if not isinstance(value, str):
                    continue
                defs.setdefault(key, value)
                if value.startswith("#"):
                    refs.add(value[1:])
            for texture in face_refs(data):
                if texture.startswith("#"):
                    refs.add(texture[1:])
        for model_id, data in models:
            parent = data.get("parent")
            if isinstance(parent, str) and lookup(parent) is None:
                problems.append(f"missing model: {parent}  (parent of {model_id})")

        def resolve(name: str, seen: set[str]) -> str | None:
            if name in seen or name not in defs:
                return None
            value = defs[name]
            if value.startswith("#"):
                return resolve(value[1:], seen | {name})
            return value

        for name in sorted(refs):
            if resolve(name, set()) is None:
                problems.append(f"missing texture reference: #{name}  (chain of {root_id})")
        for value in sorted({v for v in defs.values() if not v.startswith("#")}):
            ns, tex = parse_id(value)
            exists = tex in (vanilla_textures if ns == "minecraft" else our_textures)
            if not exists:
                problems.append(f"missing texture: {value}  (chain of {root_id})")
        checked.update(model_id for model_id, _ in models)

    for model_id in sorted(roots):
        validate_root(model_id, "blockstate/item definition")

    if problems:
        print(f"validate_models: {len(problems)} problem(s)")
        for problem in sorted(set(problems)):
            print("  " + problem)
        return 1
    print(f"validate_models: OK ({len(roots)} roots, {len(checked)} models checked)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
