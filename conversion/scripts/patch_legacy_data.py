#!/usr/bin/env python3
"""Idempotent 26.2 data-schema patches for vendored jars.

Two upstream mods ship data JSON that predates the 26.2 codecs:

1. true_ending 1.1.4d+26.2 `predicate/time/*.json`: `minecraft:time_check`
   gained a required `clock` field (TimeCheck.MAP_CODEC reads
   WorldClock.CODEC.fieldOf("clock")). Without it the predicate fails with
   "No key clock in MapLike[...]" and the advancement referencing it breaks.
2. true_ending + natures_spirit advancements: entity predicates use the
   legacy `type` key; 26.2 dispatches on the `entity_type` sub-predicate
   registry (`minecraft:entity_type`), so `type` fails with
   "Unknown registry key ... entity_sub_predicate_type: minecraft:type".

Only `data/true_ending/**` and `data/natures_spirit/**` entries are touched;
their recipes already use flat 26.2 ingredients (verified against the CGT
log). Rewrites jars in place, including dist/Prism copies, so a hand deploy
cannot ship unpatched bytes.

Run automatically from build_pack.py and run_client_gametests.sh; also safe
standalone:

  python3 conversion/scripts/patch_legacy_data.py [conversion_root]
"""

from __future__ import annotations

import json
import sys
import zipfile
from pathlib import Path

NAMESPACE_PREFIXES = ("data/true_ending/", "data/natures_spirit/")
JAR_GLOBS = ("tru.e-ending*.jar", "true_ending*.jar", "natures_spirit*.jar")
OVERWORLD = "minecraft:overworld"


def fix_time_checks(node) -> bool:
    """Add the required overworld clock to legacy time_check predicates."""
    changed = False
    if isinstance(node, dict):
        if node.get("condition") == "minecraft:time_check" and "clock" not in node:
            node["clock"] = OVERWORLD
            changed = True
        for value in node.values():
            if isinstance(value, (dict, list)):
                changed = fix_time_checks(value) or changed
    elif isinstance(node, list):
        for value in node:
            changed = fix_time_checks(value) or changed
    return changed


def fix_entity_predicates(node, parent_key: str | None = None) -> bool:
    """Rename legacy entity predicate `type` to `minecraft:entity_type`.

    26.2 dispatches EntityPredicate fields through the
    ENTITY_SUB_PREDICATE_TYPE registry, so both the flat 1.20 form
    (`entity: {type: ...}`) and the 1.21 form
    (`condition: minecraft:entity_properties` + `predicate: {type: ...}`)
    must use `minecraft:entity_type`.

    Scoped strictly to entity predicates: block predicates
    (`minecraft:would_survive` etc.) legitimately keep their own `type`
    field and a blanket rename corrupts worldgen configs.
    """
    changed = False
    if isinstance(node, dict):
        if node.get("condition") == "minecraft:entity_properties":
            predicate = node.get("predicate")
            if isinstance(predicate, dict) and isinstance(predicate.get("type"), str):
                predicate["minecraft:entity_type"] = predicate.pop("type")
                changed = True
        if parent_key == "entity" and isinstance(node.get("type"), str):
            node["minecraft:entity_type"] = node.pop("type")
            changed = True
        for key, value in list(node.items()):
            if isinstance(value, (dict, list)):
                changed = fix_entity_predicates(value, key) or changed
    elif isinstance(node, list):
        for value in node:
            changed = fix_entity_predicates(value, parent_key) or changed
    return changed


def patch_json(raw: bytes) -> tuple[bytes, str]:
    try:
        data = json.loads(raw)
    except json.JSONDecodeError:
        return raw, "skip"
    changed = fix_time_checks(data)
    changed = fix_entity_predicates(data) or changed
    if not changed:
        return raw, "already"
    return (json.dumps(data, indent=2) + "\n").encode(), "patched"


def patch_legacy_jar(path: Path) -> str:
    """Patch one jar in place. Returns 'patched'|'already'|'skip'."""
    if not path.is_file() or path.suffix != ".jar":
        return "skip"
    try:
        with zipfile.ZipFile(path, "r") as z:
            replacements: dict[str, bytes] = {}
            for name in z.namelist():
                if not name.endswith(".json"):
                    continue
                if not name.startswith(NAMESPACE_PREFIXES):
                    continue
                patched, status = patch_json(z.read(name))
                if status == "patched":
                    replacements[name] = patched
            if not replacements:
                return "already"
            items = []
            for info in z.infolist():
                data = replacements.get(info.filename) or z.read(info.filename)
                items.append((info, data))
    except zipfile.BadZipFile:
        return "skip"

    tmp = path.with_suffix(path.suffix + ".tmp")
    with zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as out:
        for info, data in items:
            ni = zipfile.ZipInfo(info.filename, date_time=info.date_time)
            ni.compress_type = info.compress_type
            ni.external_attr = info.external_attr
            ni.create_system = info.create_system
            out.writestr(ni, data)
    tmp.replace(path)
    return "patched"


def _candidate_roots(conversion_root: Path, extra: list[Path]) -> list[Path]:
    roots = [
        conversion_root / "vendored",
        conversion_root / "build" / "dist" / "server" / "mods",
        conversion_root / "build" / "dist" / "client" / "mods",
        conversion_root / "dist" / "server" / "mods",
        conversion_root / "dist" / "client" / "mods",
        *extra,
    ]
    prism = Path.home() / "Library" / "Application Support" / "PrismLauncher" / "instances"
    for inst in ("Hearthwind-Full", "Hearthwind-Minimal", "Hearthwind-Dev-Client"):
        mods = prism / inst / "minecraft" / "mods"
        if mods.is_dir():
            roots.append(mods)
    return [r for r in roots if r.is_dir()]


def patch_all(conversion_root: Path | None = None, extra_roots: list[Path] | None = None) -> dict[str, int]:
    if conversion_root is None:
        conversion_root = Path(__file__).resolve().parents[1]
    counts = {"patched": 0, "already": 0, "skip": 0}
    seen: set[Path] = set()
    for root_dir in _candidate_roots(conversion_root, extra_roots or []):
        for pattern in JAR_GLOBS:
            for jar in sorted(root_dir.glob(pattern)):
                key = jar.resolve()
                if key in seen:
                    continue
                seen.add(key)
                status = patch_legacy_jar(jar)
                counts[status] = counts.get(status, 0) + 1
                if status == "patched":
                    label = (
                        str(jar.relative_to(conversion_root))
                        if jar.is_relative_to(conversion_root)
                        else str(jar)
                    )
                    print(f"  patch_legacy_data: patched {label}")
    return counts


def main(argv: list[str]) -> int:
    root = Path(argv[1]).resolve() if len(argv) > 1 else None
    extra = [Path(a).resolve() for a in argv[2:]]
    counts = patch_all(root, extra)
    total = counts["patched"] + counts["already"]
    print(
        f"patch_legacy_data: {counts['patched']} patched, "
        f"{counts['already']} already ok, {counts['skip']} skipped"
    )
    return 0 if total > 0 else 1


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
